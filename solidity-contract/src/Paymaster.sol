// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import {IPaymaster, ExecutionResult, PAYMASTER_VALIDATION_SUCCESS_MAGIC} from "@matterlabs/zksync-contracts/contracts/system-contracts/interfaces/IPaymaster.sol";
import {IPaymasterFlow} from "@matterlabs/zksync-contracts/contracts/system-contracts/interfaces/IPaymasterFlow.sol";
import {TransactionHelper, Transaction} from "@matterlabs/zksync-contracts/contracts/system-contracts/libraries/TransactionHelper.sol";

import "@matterlabs/zksync-contracts/contracts/system-contracts/Constants.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/token/ERC20/extensions/ERC20Votes.sol";

/// @author JakeWeb3
/// @notice 투표기능을 보장하는 가스리스 아키텍처

contract GovernancePaymaster is IPaymaster, Ownable {
    /* ========== State Variables ========== */
    address public immutable governanceToken;
    
    // Gas limits and pricing
    uint256 public maxGasPrice = 50 * 10**9; // 50 Gwei
    uint256 public maxGasLimit = 1_000_000;
    uint256 public minVotingPower = 1e18; // Minimum 1 token to use paymaster
    
    // Control flags
    bool public paused = false;
    
    // Statistics tracking
    uint256 public totalTransactions;
    uint256 public totalGasPaid;
    mapping(address => uint256) public userTransactionCount;
    mapping(address => uint256) public userGasPaid;
    
    // User management
    mapping(address => bool) public blockedUsers;

    /* ========== Events ========== */
    event GasPaid(address indexed user, uint256 amount, bytes32 txHash);
    event PaymasterPaused(bool paused);
    event ParametersUpdated(uint256 maxGasPrice, uint256 maxGasLimit, uint256 minVotingPower);
    event FundsWithdrawn(address indexed owner, uint256 amount);
    event UserBlocked(address indexed user);
    event UserUnblocked(address indexed user);
    event VotingPowerRequirementUpdated(uint256 oldRequirement, uint256 newRequirement);

    /* ========== Modifiers ========== */
    modifier onlyBootloader() {
        require(
            msg.sender == BOOTLOADER_FORMAL_ADDRESS,
            "Only bootloader can call this method"
        );
        _;
    }

    modifier whenNotPaused() {
        require(!paused, "Paymaster is paused");
        _;
    }

    /// @param _governanceToken Address of the governance token contract
    constructor(address _governanceToken) Ownable(msg.sender) {
        require(_governanceToken != address(0), "Invalid governance token");
        governanceToken = _governanceToken;
    }

    /* ========== IPaymaster Implementation ========== */
    /// @dev Main paymaster validation and payment function following Matter Labs patterns
    function validateAndPayForPaymasterTransaction(
        bytes32 _txHash,
        bytes32, // unused parameter required by interface
        Transaction calldata _transaction
    ) external payable override onlyBootloader whenNotPaused returns (bytes4 magic, bytes memory context) {
        // Default to success magic
        magic = PAYMASTER_VALIDATION_SUCCESS_MAGIC;
        
        // Validate paymaster input length
        require(
            _transaction.paymasterInput.length >= 4,
            "Paymaster input too short"
        );

        // Extract paymaster flow selector
        bytes4 paymasterInputSelector = bytes4(_transaction.paymasterInput[0:4]);
        
        // Only support general paymaster flow (like Matter Labs examples)
        if (paymasterInputSelector == IPaymasterFlow.general.selector) {
            
            // 1. Validate user eligibility
            address userAddress = address(uint160(_transaction.from));
            require(!blockedUsers[userAddress], "User is blocked");
            
            // 2. Validate target contract
            require(address(uint160(_transaction.to)) == governanceToken, "Invalid target contract");
            
            // 3. Validate gas parameters
            require(_transaction.gasLimit <= maxGasLimit, "Gas limit too high");
            require(_transaction.maxFeePerGas <= maxGasPrice, "Gas price too high");
            
            // 4. Check user has minimum voting power (governance requirement)
            uint256 userVotingPower = ERC20Votes(governanceToken).getVotes(userAddress);
            require(userVotingPower >= minVotingPower, "Insufficient voting power");
            
            // 5. Validate transaction data and function selector
            require(_transaction.data.length >= 4, "Transaction data too short");
            bytes4 functionSelector = bytes4(_transaction.data[0:4]);
            require(isAllowedGovernanceFunction(functionSelector), "Function not allowed");
            
            // 6. Calculate required ETH for gas
            uint256 requiredETH = _transaction.gasLimit * _transaction.maxFeePerGas;
            require(address(this).balance >= requiredETH, "Insufficient paymaster balance");
            
            // 7. Pay the bootloader
            (bool success, ) = payable(BOOTLOADER_FORMAL_ADDRESS).call{value: requiredETH}("");
            require(success, "Failed to transfer fee to bootloader");
            
            // 8. Update statistics
            unchecked {
                totalTransactions++;
                totalGasPaid += requiredETH;
                userTransactionCount[userAddress]++;
                userGasPaid[userAddress] += requiredETH;
            }
            
            // 9. Emit event
            emit GasPaid(userAddress, requiredETH, _txHash);
            
        } else {
            revert("Unsupported paymaster flow");
        }
        
        return (magic, "");
    }

    /// @dev Required postTransaction function (part of IPaymaster interface)
    function postTransaction(
        bytes calldata _context,
        Transaction calldata _transaction,
        bytes32,
        bytes32,
        ExecutionResult _txResult,
        uint256 _maxRefundedGas
    ) external payable override onlyBootloader {
        // Post-transaction logic can be implemented here
        // Currently, refunds are not supported (following Matter Labs examples)
    }

    /* ========== Governance Function Validation ========== */
    /// @dev Check if function selector is allowed for gasless transactions
    function isAllowedGovernanceFunction(bytes4 selector) public pure returns (bool) {
        return selector == bytes4(keccak256("vote(uint256,bool)")) ||
               selector == bytes4(keccak256("createProposal(string,uint256)")) ||
               selector == bytes4(keccak256("delegateVoting(address)")) ||
               selector == bytes4(keccak256("delegate(address)"));
    }

    /* ========== Admin Functions ========== */
    /// @dev Pause the paymaster (emergency stop)
    function pause() external onlyOwner {
        paused = true;
        emit PaymasterPaused(true);
    }

    /// @dev Unpause the paymaster
    function unpause() external onlyOwner {
        paused = false;
        emit PaymasterPaused(false);
    }

    /// @dev Update gas parameters and voting power requirement
    function updateParameters(
        uint256 _maxGasPrice, 
        uint256 _maxGasLimit,
        uint256 _minVotingPower
    ) external onlyOwner {
        require(_maxGasPrice > 0, "Gas price must be > 0");
        require(_maxGasLimit > 0, "Gas limit must be > 0");
        require(_minVotingPower > 0, "Min voting power must be > 0");
        
        maxGasPrice = _maxGasPrice;
        maxGasLimit = _maxGasLimit;
        minVotingPower = _minVotingPower;
        
        emit ParametersUpdated(_maxGasPrice, _maxGasLimit, _minVotingPower);
    }

    /// @dev Update minimum voting power requirement
    function updateMinVotingPower(uint256 _newMinVotingPower) external onlyOwner {
        require(_newMinVotingPower > 0, "Min voting power must be > 0");
        uint256 oldRequirement = minVotingPower;
        minVotingPower = _newMinVotingPower;
        emit VotingPowerRequirementUpdated(oldRequirement, _newMinVotingPower);
    }

    /// @dev Withdraw specific amount (following Matter Labs pattern)
    function withdraw(address payable _to) external onlyOwner {
        require(_to != address(0), "Invalid recipient");
        uint256 balance = address(this).balance;
        require(balance > 0, "No funds to withdraw");
        
        (bool success, ) = _to.call{value: balance}("");
        require(success, "Failed to withdraw funds");
        
        emit FundsWithdrawn(_to, balance);
    }

    /// @dev Block user from using paymaster
    function blockUser(address user) external onlyOwner {
        require(user != address(0), "Invalid user address");
        blockedUsers[user] = true;
        emit UserBlocked(user);
    }

    /// @dev Unblock user 
    function unblockUser(address user) external onlyOwner {
        require(user != address(0), "Invalid user address");
        blockedUsers[user] = false;
        emit UserUnblocked(user);
    }

    /* ========== View Functions ========== */
    /// @dev Get paymaster balance
    function getBalance() external view returns (uint256) {
        return address(this).balance;
    }

    /// @dev Get comprehensive paymaster statistics
    function getStats() external view returns (
        uint256 _totalTransactions,
        uint256 _totalGasPaid,
        uint256 _currentBalance,
        bool _isPaused
    ) {
        return (totalTransactions, totalGasPaid, address(this).balance, paused);
    }

    /// @dev Get user-specific statistics
    function getUserStats(address user) external view returns (
        uint256 transactionCount,
        uint256 userTotalGasPaid
    ) {
        return (userTransactionCount[user], userGasPaid[user]);
    }

    /// @dev Get current parameters
    function getParameters() external view returns (
        uint256 _maxGasPrice, 
        uint256 _maxGasLimit,
        uint256 _minVotingPower
    ) {
        return (maxGasPrice, maxGasLimit, minVotingPower);
    }

    /// @dev Check if user is eligible to use paymaster
    function isEligible(address user) external view returns (bool) {
        if (blockedUsers[user] || paused) return false;
        
        uint256 votingPower = ERC20Votes(governanceToken).getVotes(user);
        return votingPower >= minVotingPower;
    }

    /// @dev Check if function selector is supported
    function isFunctionSupported(bytes4 selector) external pure returns (bool) {
        return isAllowedGovernanceFunction(selector);
    }

    /* ========== Receive ETH ========== */
    /// @dev Allow contract to receive ETH for gas payments
    receive() external payable {
        // Contract can receive ETH for funding
    }
}