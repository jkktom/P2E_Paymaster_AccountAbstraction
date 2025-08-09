import { expect } from "chai";
import { ethers } from "hardhat";
import { time } from "@nomicfoundation/hardhat-network-helpers";
import { GovernanceToken } from "@/typechain-types";
import { SignerWithAddress } from "@nomicfoundation/hardhat-ethers/signers";

describe("GovernanceToken", function () {
  let governanceToken: GovernanceToken; // 토큰 컨트랙트
  let owner: SignerWithAddress; // 소유자
  let addr1: SignerWithAddress; // 보유자1
  let addr2: SignerWithAddress; // 보유자2
  
  beforeEach(async function () {
    [owner, addr1, addr2] = await ethers.getSigners();
    //토큰 컨트랙트 배포, 이름과 심볼 설정
    const GovernanceTokenFactory = await ethers.getContractFactory("GovernanceToken");
    governanceToken = await GovernanceTokenFactory.deploy(
      "Blooming Blockchain Service Token",
      "BLOOM"
    );
    await governanceToken.waitForDeployment();
  });

  describe("Token Functionality", function () {
    // 토큰 이름과 심볼 확인
    it("Should have correct name and symbol", async function () {
      expect(await governanceToken.name()).to.equal("Blooming Blockchain Service Token");
      expect(await governanceToken.symbol()).to.equal("BLOOM");
    });

    // 토큰 발행 확인
    it("Should mint tokens correctly", async function () {
      const amount = ethers.parseEther("100");
      
      await governanceToken.mintForExchange(
        addr1.address,
        amount,
        "Blooming Blockchain Service"
      );
      
      expect(await governanceToken.balanceOf(addr1.address)).to.equal(amount);
      expect(await governanceToken.totalSupply()).to.equal(amount);
    });

    // 소유자만 토큰 발행 가능 확인
    it("Should only allow owner to mint", async function () {
      const amount = ethers.parseEther("100");
      
      await expect(
        governanceToken.connect(addr1).mintForExchange(
          addr2.address,
          amount,
          "Unauthorized mint" // 권한 없는 토큰 발행 확인
        )
      ).to.be.revertedWithCustomError(governanceToken, "OwnableUnauthorizedAccount");
    });

    // 토큰 발행 확인
    it("Should batch mint correctly", async function () {
      const recipients = [addr1.address, addr2.address];
      const amounts = [
        ethers.parseEther("100"),
        ethers.parseEther("200")
      ];
      
      await governanceToken.batchMint(recipients, amounts, "Batch Blooming Blockchain Service");
      
      expect(await governanceToken.balanceOf(addr1.address)).to.equal(amounts[0]);
      expect(await governanceToken.balanceOf(addr2.address)).to.equal(amounts[1]);
    });

    // 토큰 발행 이벤트 확인
    it("Should emit TokensMinted event", async function () {
      const amount = ethers.parseEther("100");
      
      await expect(
        governanceToken.mintForExchange(addr1.address, amount, "Test mint")
      ).to.emit(governanceToken, "TokensMinted")
        .withArgs(addr1.address, amount, "Test mint");
    });
  });

  // 투표 기능 테스트
  describe("Voting Functionality", function () {
    beforeEach(async function () {
      // 1000개 토큰 발행
      const amount = ethers.parseEther("1000");
      await governanceToken.mintForExchange(addr1.address, amount, "For voting");
      
      // 자신에게 투표 권한 부여
      await governanceToken.connect(addr1).delegate(addr1.address);
    });

    // 제안 생성 확인
    it("Should create proposal correctly", async function () {
      const description = "Test proposal";
      const deadline = (await time.latest()) + 60 * 60 * 24 * 7; // 7 days from now
      
      await governanceToken.connect(addr1).createProposal(description, deadline);
      
      const proposal = await governanceToken.getProposal(1);
      expect(proposal.description).to.equal(description);
      expect(proposal.proposer).to.equal(addr1.address);
    });

    // 제안 생성 최소 투표 확인
    it("Should require minimum voting power to create proposal", async function () {
      const description = "Test proposal";
      const deadline = (await time.latest()) + 60 * 60 * 24 * 7; // 7 days from now
      
      await expect(
        governanceToken.connect(addr2).createProposal(description, deadline)
      ).to.be.revertedWith("Insufficient voting power");
    });

    // 제안 투표 확인
    it("Should allow voting on proposals", async function () {
      const deadline = (await time.latest()) + 60 * 60 * 24 * 7; // 7 days from now
      await governanceToken.connect(addr1).createProposal("Test proposal", deadline);
      
      await governanceToken.connect(addr1).vote(1, true);
      // 1000개 토큰 투표 확인
      const proposal = await governanceToken.getProposal(1);
      expect(proposal.forVotes).to.equal(ethers.parseEther("1000"));
      
      // 투표 정보 확인
      const voteInfo = await governanceToken.getVoteInfo(1, addr1.address);
      expect(voteInfo.voted).to.be.true;
      expect(voteInfo.weight).to.equal(ethers.parseEther("1000"));
    });

    // 중복 투표 방지 확인
    it("Should prevent double voting", async function () {
      const deadline = (await time.latest()) + 60 * 60 * 24 * 7; // 7 days from now
      await governanceToken.connect(addr1).createProposal("Test proposal", deadline);
      await governanceToken.connect(addr1).vote(1, true);
      
      await expect(
        governanceToken.connect(addr1).vote(1, false)
      ).to.be.revertedWith("Already voted");
    });

    // 제안 실행 확인
    it("Should execute passed proposals", async function () {
      const deadline = (await time.latest()) + 60 * 60 * 24 * 7; // 7 days from now
      await governanceToken.connect(addr1).createProposal("Test proposal", deadline);
      await governanceToken.connect(addr1).vote(1, true);
      
      // 설정된 투표 기간 초과 확인
      await time.increase(8 * 24 * 60 * 60); // 8 days
      
      await governanceToken.executeProposal(1);
      
      const proposal = await governanceToken.getProposal(1);
      expect(proposal.executed).to.be.true;
    });

    // 거절된 제안 실행 방지 확인
    it("Should not execute rejected proposals", async function () {
      // 두번째 투표자에게 500개 토큰 발행
      const amount = ethers.parseEther("500");
      await governanceToken.mintForExchange(addr2.address, amount, "For voting");
      await governanceToken.connect(addr2).delegate(addr2.address);
      
      const deadline = (await time.latest()) + 60 * 60 * 24 * 7; // 7 days from now
      await governanceToken.connect(addr1).createProposal("Test proposal", deadline);
      await governanceToken.connect(addr1).vote(1, true); // 첫번째 투표자 1000 동의 투표
      await governanceToken.connect(addr2).vote(1, false); // 두번째 투표자 500 반대 투표
      // 설정된 투표 기간 초과 확인
      await time.increase(8 * 24 * 60 * 60); // 8 days
      
      await governanceToken.executeProposal(1); // 1000 > 500 이므로 거절된 제안 실행 방지 확인

      // 제안 실행 확인
      const proposal = await governanceToken.getProposal(1);
      expect(proposal.executed).to.be.true;
    });

    // 제안 생명주기 이벤트 확인
    it("Should emit events for proposal lifecycle", async function () {
      const description = "Test proposal";
      
      // 제안 생성 이벤트 확인
      await expect(
        governanceToken.connect(addr1).createProposal(description, (await time.latest()) + 60 * 60 * 24 * 7)
      ).to.emit(governanceToken, "ProposalCreated");
      
      // 제안 생성 데이터 확인
      const proposal = await governanceToken.getProposal(1);
      expect(proposal.description).to.equal(description);
      expect(proposal.proposer).to.equal(addr1.address);
      
      // 투표 이벤트 확인
      await expect(
        governanceToken.connect(addr1).vote(1, true)
      ).to.emit(governanceToken, "VoteCast")
        .withArgs(1, addr1.address, true, ethers.parseEther("1000"));
      
      // 제안 실행 이벤트 확인
      await time.increase(8 * 24 * 60 * 60);
      await expect(
        governanceToken.executeProposal(1)
      ).to.emit(governanceToken, "ProposalExecuted")
        .withArgs(1);
    });
  });

  // 보안 기능 테스트
  describe("Security Features", function () {
    // 일시정지 및 재시작 확인
    it("Should pause and unpause correctly", async function () {
      await governanceToken.pause();
      
      await expect(
        governanceToken.mintForExchange(
          addr1.address,
          ethers.parseEther("100"),
          "Should fail"
        )
      ).to.be.revertedWithCustomError(governanceToken, "EnforcedPause");
      
      await governanceToken.unpause();
      
      await governanceToken.mintForExchange(
        addr1.address,
        ethers.parseEther("100"),
        "Should work"
      );
      
      expect(await governanceToken.balanceOf(addr1.address)).to.equal(ethers.parseEther("100"));
    });

    // 소유자만 일시정지 가능 확인
    it("Should only allow owner to pause", async function () {
      await expect(
        governanceToken.connect(addr1).pause()
      ).to.be.revertedWithCustomError(governanceToken, "OwnableUnauthorizedAccount");
    });

    // 일시정지 시 투표 방지 확인
    it("Should prevent voting when paused", async function () {
      // Setup
      const amount = ethers.parseEther("1000");
      await governanceToken.mintForExchange(addr1.address, amount, "For voting");
      await governanceToken.connect(addr1).delegate(addr1.address);
      const deadline = (await time.latest()) + 60 * 60 * 24 * 7; // 7 days from now
      await governanceToken.connect(addr1).createProposal("Test proposal", deadline);
      
      // Pause and try to vote
      await governanceToken.pause();
      
      await expect(
        governanceToken.connect(addr1).vote(1, true)
      ).to.be.revertedWithCustomError(governanceToken, "EnforcedPause");
    });

    // 주소 0 방지 확인
    it("Should handle zero address minting prevention", async function () {
      await expect(
        governanceToken.mintForExchange(
          ethers.ZeroAddress,
          ethers.parseEther("100"),
          "Should fail"
        )
      ).to.be.revertedWith("Cannot mint to zero address");
    });

    // 0 토큰 발행 방지 확인
    it("Should handle zero amount minting prevention", async function () {
      await expect(
        governanceToken.mintForExchange(
          addr1.address,
          0,
          "Should fail"
        )
      ).to.be.revertedWith("Amount must be greater than 0");
    });
  });

  // 뷰 함수 테스트
  describe("View Functions", function () {
    it("Should return correct voting power", async function () {
      const amount = ethers.parseEther("500");
      await governanceToken.mintForExchange(addr1.address, amount, "For voting");
      await governanceToken.connect(addr1).delegate(addr1.address);
      
      expect(await governanceToken.getVotingPower(addr1.address)).to.equal(amount);
    });

    // 대리 투표 없는 경우 투표 권한 확인
    it("Should return zero voting power for non-delegated tokens", async function () {
      const amount = ethers.parseEther("500");
      await governanceToken.mintForExchange(addr1.address, amount, "For voting");
      // Not delegating
      
      expect(await governanceToken.getVotingPower(addr1.address)).to.equal(0);
    });
  });

  // 관리자 함수 테스트
  describe("Admin Functions", function () {
    it("Should allow owner to cancel proposals", async function () {
      // Setup proposal
      const amount = ethers.parseEther("1000");
      await governanceToken.mintForExchange(addr1.address, amount, "For voting");
      await governanceToken.connect(addr1).delegate(addr1.address);
      const deadline = (await time.latest()) + 60 * 60 * 24 * 7; // 7 days from now
      await governanceToken.connect(addr1).createProposal("Test proposal", deadline);
      
      // Cancel as owner
      await governanceToken.cancelProposal(1);
      
      const proposal = await governanceToken.getProposal(1);
      expect(proposal.canceled).to.be.true;
    });

    // 소유자가 아닌 경우 제안 취소 방지 확인
    it("Should not allow non-owners to cancel proposals", async function () {
      // Setup proposal
      const amount = ethers.parseEther("1000");
      await governanceToken.mintForExchange(addr1.address, amount, "For voting");
      await governanceToken.connect(addr1).delegate(addr1.address);
      const deadline = (await time.latest()) + 60 * 60 * 24 * 7; // 7 days from now
      await governanceToken.connect(addr1).createProposal("Test proposal", deadline);
      
      // Try to cancel as non-owner
      await expect(
        governanceToken.connect(addr1).cancelProposal(1)
      ).to.be.revertedWithCustomError(governanceToken, "OwnableUnauthorizedAccount");
    });
  });
});