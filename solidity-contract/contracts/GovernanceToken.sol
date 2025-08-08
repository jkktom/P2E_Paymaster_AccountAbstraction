// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";
import "@openzeppelin/contracts/token/ERC20/extensions/ERC20Votes.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/Pausable.sol";
import "@openzeppelin/contracts/utils/ReentrancyGuard.sol";
import "@openzeppelin/contracts/utils/cryptography/EIP712.sol";

/**
 * @title GovernanceToken
 * @dev ERC20 token with built-in voting functionality for blockchain exchange service
 * 
 * Features:
 * - Standard ERC20 functionality
 * - Voting power delegation and tracking
 * - Proposal creation and voting system
 * - Backend-controlled minting for point exchange
 * - Pausable for emergency situations
 */
contract GovernanceToken is ERC20, ERC20Votes, Ownable, Pausable, ReentrancyGuard {
    
    // ============= Constants =============
    uint256 public constant VOTING_PERIOD = 7 days;
    uint256 public constant MIN_VOTING_POWER = 1e18; // 1 token minimum
    
    // ============= State Variables =============
    uint256 public proposalCount;
    mapping(uint256 => Proposal) public proposals;
    mapping(uint256 => mapping(address => bool)) public hasVoted;
    mapping(uint256 => mapping(address => uint256)) public votePower;
    
    // ============= Events =============
    event ProposalCreated(
        uint256 indexed proposalId,
        address indexed proposer,
        string description,
        uint256 deadline
    );
    
    event VoteCast(
        uint256 indexed proposalId,
        address indexed voter,
        bool support,
        uint256 weight
    );
    
    event ProposalExecuted(uint256 indexed proposalId);
    
    event TokensMinted(address indexed to, uint256 amount, string reason);
    
    // ============= Structs =============
    struct Proposal {
        string description;
        address proposer;
        uint256 forVotes;
        uint256 againstVotes;
        uint256 deadline;
        bool executed;
        bool canceled;
        uint256 createdAt;
    }
    
    // ============= Constructor =============
    constructor(
        string memory name,
        string memory symbol
    ) ERC20(name, symbol) EIP712(name, "1") Ownable(msg.sender) {
        // Initial setup - owner is the backend service
    }
    
    // ============= Minting Functions =============
    /**
     * @dev Mint tokens for point-to-token exchange
     * Only callable by backend service (owner)
     * @param to Address to mint tokens to
     * @param amount Amount of tokens to mint
     * @param reason Reason for minting (for audit trail)
     */
    function mintForExchange(
        address to,
        uint256 amount,
        string calldata reason
    ) external onlyOwner whenNotPaused nonReentrant {
        require(to != address(0), "Cannot mint to zero address");
        require(amount > 0, "Amount must be greater than 0");
        
        _mint(to, amount);
        emit TokensMinted(to, amount, reason);
    }
    
    /**
     * @dev Batch mint tokens for multiple users
     * Gas-efficient for multiple exchanges
     */
    function batchMint(
        address[] calldata recipients,
        uint256[] calldata amounts,
        string calldata reason
    ) external onlyOwner whenNotPaused nonReentrant {
        require(recipients.length == amounts.length, "Arrays length mismatch");
        require(recipients.length > 0, "Empty arrays");
        
        for (uint256 i = 0; i < recipients.length; i++) {
            require(recipients[i] != address(0), "Cannot mint to zero address");
            require(amounts[i] > 0, "Amount must be greater than 0");
            
            _mint(recipients[i], amounts[i]);
        }
        
        emit TokensMinted(address(0), 0, reason); // Batch event
    }
    
    // ============= Voting Functions =============
    /**
     * @dev Create a new proposal
     * Requires minimum voting power
     */
    function createProposal(string calldata description) external whenNotPaused {
        require(getVotes(msg.sender) >= MIN_VOTING_POWER, "Insufficient voting power");
        require(bytes(description).length > 0, "Description cannot be empty");
        
        proposalCount++;
        uint256 proposalId = proposalCount;
        
        proposals[proposalId] = Proposal({
            description: description,
            proposer: msg.sender,
            forVotes: 0,
            againstVotes: 0,
            deadline: block.timestamp + VOTING_PERIOD,
            executed: false,
            canceled: false,
            createdAt: block.timestamp
        });
        
        emit ProposalCreated(proposalId, msg.sender, description, block.timestamp + VOTING_PERIOD);
    }
    
    /**
     * @dev Vote on a proposal
     */
    function vote(uint256 proposalId, bool support) external whenNotPaused {
        require(proposalId > 0 && proposalId <= proposalCount, "Invalid proposal ID");
        require(!hasVoted[proposalId][msg.sender], "Already voted");
        
        Proposal storage proposal = proposals[proposalId];
        require(!proposal.executed, "Proposal already executed");
        require(!proposal.canceled, "Proposal canceled");
        require(block.timestamp < proposal.deadline, "Voting period ended");
        
        uint256 weight = getVotes(msg.sender);
        require(weight > 0, "No voting power");
        
        hasVoted[proposalId][msg.sender] = true;
        votePower[proposalId][msg.sender] = weight;
        
        if (support) {
            proposal.forVotes += weight;
        } else {
            proposal.againstVotes += weight;
        }
        
        emit VoteCast(proposalId, msg.sender, support, weight);
    }
    
    /**
     * @dev Execute a passed proposal
     */
    function executeProposal(uint256 proposalId) external whenNotPaused {
        require(proposalId > 0 && proposalId <= proposalCount, "Invalid proposal ID");
        
        Proposal storage proposal = proposals[proposalId];
        require(!proposal.executed, "Proposal already executed");
        require(!proposal.canceled, "Proposal canceled");
        require(block.timestamp >= proposal.deadline, "Voting period not ended");
        require(proposal.forVotes > proposal.againstVotes, "Proposal not passed");
        
        proposal.executed = true;
        emit ProposalExecuted(proposalId);
    }
    
    /**
     * @dev Cancel a proposal (owner only)
     */
    function cancelProposal(uint256 proposalId) external onlyOwner {
        require(proposalId > 0 && proposalId <= proposalCount, "Invalid proposal ID");
        
        Proposal storage proposal = proposals[proposalId];
        require(!proposal.executed, "Cannot cancel executed proposal");
        
        proposal.canceled = true;
    }
    
    // ============= View Functions =============
    /**
     * @dev Get proposal details
     */
    function getProposal(uint256 proposalId) external view returns (
        string memory description,
        address proposer,
        uint256 forVotes,
        uint256 againstVotes,
        uint256 deadline,
        bool executed,
        bool canceled,
        uint256 createdAt
    ) {
        require(proposalId > 0 && proposalId <= proposalCount, "Invalid proposal ID");
        
        Proposal storage proposal = proposals[proposalId];
        return (
            proposal.description,
            proposal.proposer,
            proposal.forVotes,
            proposal.againstVotes,
            proposal.deadline,
            proposal.executed,
            proposal.canceled,
            proposal.createdAt
        );
    }
    
    /**
     * @dev Check if address has voted on proposal
     */
    function getVoteInfo(uint256 proposalId, address voter) external view returns (
        bool voted,
        uint256 weight
    ) {
        return (hasVoted[proposalId][voter], votePower[proposalId][voter]);
    }
    
    /**
     * @dev Get voting power of an address
     */
    function getVotingPower(address account) external view returns (uint256) {
        return getVotes(account);
    }
    
    // ============= Admin Functions =============
    /**
     * @dev Pause contract in emergency
     */
    function pause() external onlyOwner {
        _pause();
    }
    
    /**
     * @dev Unpause contract
     */
    function unpause() external onlyOwner {
        _unpause();
    }
    
    // ============= Required Overrides =============
    function _update(
        address from,
        address to,
        uint256 value
    ) internal override(ERC20, ERC20Votes) {
        super._update(from, to, value);
    }
}