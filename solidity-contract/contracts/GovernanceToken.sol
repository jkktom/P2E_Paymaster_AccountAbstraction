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
 * @dev 투표 기능이 내장된 ERC20 토큰
 * 
 * Features:
 * - ERC20 기능 구현
 * - 투표 권한 부여 및 추적
 * - 제안 생성 및 투표 시스템
 * - 백엔드 제어 토큰 발행
 * - 일시정지 기능 구현
 */
contract GovernanceToken is ERC20, ERC20Votes, Ownable, Pausable, ReentrancyGuard {
    
    // ============= 상수 선언 =============
    uint256 public constant MIN_VOTING_POWER = 1e18; // 최소 투표 권한 1 token
    
    // ============= 상태 변수 선언 =============
    uint256 public proposalCount;
    mapping(uint256 => Proposal) public proposals;
    mapping(uint256 => mapping(address => bool)) public hasVoted;
    mapping(uint256 => mapping(address => uint256)) public votePower;
    
    // ============= 이벤트 선언 =============
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
    
    // ============= 투표 제안 구조체  =============
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
    
    // ============= 생성자 =============
    constructor(
        string memory name,
        string memory symbol
    ) ERC20(name, symbol) EIP712(name, "1") Ownable(msg.sender) {
        // 초기 설정 - 소유자는 백엔드 서비스에서 발행한 토큰을 관리하는 주체
    }
    
    // ============= 토큰 발행 함수 =============
    /**
     * @dev 토큰 발행 함수
     * 백엔드 서비스(소유자)만 호출 가능
     * @param to 토큰 발행 대상 주소
     * @param amount 발행할 토큰 양
     * @param reason 발행 이유 (감사 기록용)
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
     * @dev 다수 사용자에게 토큰 발행 함수
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
    
    // ============= 투표 함수 =============
    /**
     * @dev 새로운 제안 생성 함수
     * 최소 투표 권한 필요
     */
    function createProposal(
        string calldata description,
        uint256 deadline
    ) external whenNotPaused {
        require(getVotes(msg.sender) >= MIN_VOTING_POWER, "Insufficient voting power");
        require(bytes(description).length > 0, "Description cannot be empty");
        require(deadline > block.timestamp, "Deadline must be in the future");

        proposalCount++;
        uint256 proposalId = proposalCount;
        
        proposals[proposalId] = Proposal({
            description: description,
            proposer: msg.sender,
            forVotes: 0,
            againstVotes: 0,
            deadline: deadline,
            executed: false,
            canceled: false,
            createdAt: block.timestamp
        });
        
        emit ProposalCreated(proposalId, msg.sender, description, deadline);
    }
    
    /**
     * @dev 제안에 투표 함수
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
     * @dev 통과된 제안 실행 함수
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
     * @dev 제안 취소 함수 (소유자만 호출 가능)
     */
    function cancelProposal(uint256 proposalId) external onlyOwner {
        require(proposalId > 0 && proposalId <= proposalCount, "Invalid proposal ID");
        
        Proposal storage proposal = proposals[proposalId];
        require(!proposal.executed, "Cannot cancel executed proposal");
        
        proposal.canceled = true;
    }
    
    // ============= 뷰 함수 =============
    /**
     * @dev 제안 상세 정보 조회 함수
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
     * @dev 특정 주소가 제안에 투표했는지 확인 함수
     */
    function getVoteInfo(uint256 proposalId, address voter) external view returns (
        bool voted,
        uint256 weight
    ) {
        return (hasVoted[proposalId][voter], votePower[proposalId][voter]);
    }
    
    /**
     * @dev 특정 주소의 투표 권한 조회 함수
     */
    function getVotingPower(address account) external view returns (uint256) {
        return getVotes(account);
    }
    
    // ============= 관리자 함수 =============
    /**
     * @dev Pause 일시정지 함수
     */
    function pause() external onlyOwner {
        _pause();
    }
    
    /**
     * @dev 일시정지 해제
     */
    function unpause() external onlyOwner {
        _unpause();
    }
    
    // ============= 필수 오버라이드 함수 =============
    function _update(
        address from,
        address to,
        uint256 value
    ) internal override(ERC20, ERC20Votes) {
        super._update(from, to, value);
    }
}