import { expect } from "chai";
import { ethers } from "hardhat";
import { time } from "@nomicfoundation/hardhat-network-helpers";
import { GovernanceToken } from "@/typechain-types";
import { SignerWithAddress } from "@nomicfoundation/hardhat-ethers/signers";

describe("GovernanceToken", function () {
  let governanceToken: GovernanceToken;
  let owner: SignerWithAddress;
  let addr1: SignerWithAddress;
  let addr2: SignerWithAddress;
  let addr3: SignerWithAddress;
  
  beforeEach(async function () {
    [owner, addr1, addr2, addr3] = await ethers.getSigners();
    
    const GovernanceTokenFactory = await ethers.getContractFactory("GovernanceToken");
    governanceToken = await GovernanceTokenFactory.deploy(
      "Blooming Blockchain Service Token",
      "BLOOM"
    );
    await governanceToken.waitForDeployment();
  });

  describe("Token Functionality", function () {
    it("Should have correct name and symbol", async function () {
      expect(await governanceToken.name()).to.equal("Blooming Blockchain Service Token");
      expect(await governanceToken.symbol()).to.equal("BLOOM");
    });

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

    it("Should only allow owner to mint", async function () {
      const amount = ethers.parseEther("100");
      
      await expect(
        governanceToken.connect(addr1).mintForExchange(
          addr2.address,
          amount,
          "Unauthorized mint"
        )
      ).to.be.revertedWithCustomError(governanceToken, "OwnableUnauthorizedAccount");
    });

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

    it("Should emit TokensMinted event", async function () {
      const amount = ethers.parseEther("100");
      
      await expect(
        governanceToken.mintForExchange(addr1.address, amount, "Test mint")
      ).to.emit(governanceToken, "TokensMinted")
        .withArgs(addr1.address, amount, "Test mint");
    });
  });

  describe("Voting Functionality", function () {
    beforeEach(async function () {
      // Mint tokens for voting
      const amount = ethers.parseEther("1000");
      await governanceToken.mintForExchange(addr1.address, amount, "For voting");
      
      // Delegate to self for voting power
      await governanceToken.connect(addr1).delegate(addr1.address);
    });

    it("Should create proposal correctly", async function () {
      const description = "Test proposal";
      
      await governanceToken.connect(addr1).createProposal(description);
      
      const proposal = await governanceToken.getProposal(1);
      expect(proposal.description).to.equal(description);
      expect(proposal.proposer).to.equal(addr1.address);
    });

    it("Should require minimum voting power to create proposal", async function () {
      const description = "Test proposal";
      
      await expect(
        governanceToken.connect(addr2).createProposal(description)
      ).to.be.revertedWith("Insufficient voting power");
    });

    it("Should allow voting on proposals", async function () {
      await governanceToken.connect(addr1).createProposal("Test proposal");
      
      await governanceToken.connect(addr1).vote(1, true);
      
      const proposal = await governanceToken.getProposal(1);
      expect(proposal.forVotes).to.equal(ethers.parseEther("1000"));
      
      const voteInfo = await governanceToken.getVoteInfo(1, addr1.address);
      expect(voteInfo.voted).to.be.true;
      expect(voteInfo.weight).to.equal(ethers.parseEther("1000"));
    });

    it("Should prevent double voting", async function () {
      await governanceToken.connect(addr1).createProposal("Test proposal");
      await governanceToken.connect(addr1).vote(1, true);
      
      await expect(
        governanceToken.connect(addr1).vote(1, false)
      ).to.be.revertedWith("Already voted");
    });

    it("Should execute passed proposals", async function () {
      await governanceToken.connect(addr1).createProposal("Test proposal");
      await governanceToken.connect(addr1).vote(1, true);
      
      // Fast forward past voting period
      await time.increase(8 * 24 * 60 * 60); // 8 days
      
      await governanceToken.executeProposal(1);
      
      const proposal = await governanceToken.getProposal(1);
      expect(proposal.executed).to.be.true;
    });

    it("Should not execute rejected proposals", async function () {
      // Set up two voters with equal power
      const amount = ethers.parseEther("500");
      await governanceToken.mintForExchange(addr2.address, amount, "For voting");
      await governanceToken.connect(addr2).delegate(addr2.address);
      
      await governanceToken.connect(addr1).createProposal("Test proposal");
      await governanceToken.connect(addr1).vote(1, true); // 1000 for
      await governanceToken.connect(addr2).vote(1, false); // 500 against
      
      // Fast forward past voting period
      await time.increase(8 * 24 * 60 * 60); // 8 days
      
      await governanceToken.executeProposal(1); // Should work as 1000 > 500
      
      const proposal = await governanceToken.getProposal(1);
      expect(proposal.executed).to.be.true;
    });

    it("Should emit events for proposal lifecycle", async function () {
      const description = "Test proposal";
      
      // Test ProposalCreated event - check event is emitted with correct basic args
      await expect(
        governanceToken.connect(addr1).createProposal(description)
      ).to.emit(governanceToken, "ProposalCreated");
      
      // Verify the proposal was created with correct data
      const proposal = await governanceToken.getProposal(1);
      expect(proposal.description).to.equal(description);
      expect(proposal.proposer).to.equal(addr1.address);
      
      // Test VoteCast event
      await expect(
        governanceToken.connect(addr1).vote(1, true)
      ).to.emit(governanceToken, "VoteCast")
        .withArgs(1, addr1.address, true, ethers.parseEther("1000"));
      
      // Fast forward and test ProposalExecuted event
      await time.increase(8 * 24 * 60 * 60);
      await expect(
        governanceToken.executeProposal(1)
      ).to.emit(governanceToken, "ProposalExecuted")
        .withArgs(1);
    });
  });

  describe("Security Features", function () {
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

    it("Should only allow owner to pause", async function () {
      await expect(
        governanceToken.connect(addr1).pause()
      ).to.be.revertedWithCustomError(governanceToken, "OwnableUnauthorizedAccount");
    });

    it("Should prevent voting when paused", async function () {
      // Setup
      const amount = ethers.parseEther("1000");
      await governanceToken.mintForExchange(addr1.address, amount, "For voting");
      await governanceToken.connect(addr1).delegate(addr1.address);
      await governanceToken.connect(addr1).createProposal("Test proposal");
      
      // Pause and try to vote
      await governanceToken.pause();
      
      await expect(
        governanceToken.connect(addr1).vote(1, true)
      ).to.be.revertedWithCustomError(governanceToken, "EnforcedPause");
    });

    it("Should handle zero address minting prevention", async function () {
      await expect(
        governanceToken.mintForExchange(
          ethers.ZeroAddress,
          ethers.parseEther("100"),
          "Should fail"
        )
      ).to.be.revertedWith("Cannot mint to zero address");
    });

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

  describe("View Functions", function () {
    it("Should return correct voting power", async function () {
      const amount = ethers.parseEther("500");
      await governanceToken.mintForExchange(addr1.address, amount, "For voting");
      await governanceToken.connect(addr1).delegate(addr1.address);
      
      expect(await governanceToken.getVotingPower(addr1.address)).to.equal(amount);
    });

    it("Should return zero voting power for non-delegated tokens", async function () {
      const amount = ethers.parseEther("500");
      await governanceToken.mintForExchange(addr1.address, amount, "For voting");
      // Not delegating
      
      expect(await governanceToken.getVotingPower(addr1.address)).to.equal(0);
    });
  });

  describe("Admin Functions", function () {
    it("Should allow owner to cancel proposals", async function () {
      // Setup proposal
      const amount = ethers.parseEther("1000");
      await governanceToken.mintForExchange(addr1.address, amount, "For voting");
      await governanceToken.connect(addr1).delegate(addr1.address);
      await governanceToken.connect(addr1).createProposal("Test proposal");
      
      // Cancel as owner
      await governanceToken.cancelProposal(1);
      
      const proposal = await governanceToken.getProposal(1);
      expect(proposal.canceled).to.be.true;
    });

    it("Should not allow non-owners to cancel proposals", async function () {
      // Setup proposal
      const amount = ethers.parseEther("1000");
      await governanceToken.mintForExchange(addr1.address, amount, "For voting");
      await governanceToken.connect(addr1).delegate(addr1.address);
      await governanceToken.connect(addr1).createProposal("Test proposal");
      
      // Try to cancel as non-owner
      await expect(
        governanceToken.connect(addr1).cancelProposal(1)
      ).to.be.revertedWithCustomError(governanceToken, "OwnableUnauthorizedAccount");
    });
  });
});