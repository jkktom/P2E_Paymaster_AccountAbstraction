import { expect } from "chai";
import { ethers } from "hardhat";
import { Contract, Signer } from "ethers";
import { GovernanceToken, GovernancePaymaster } from "../typechain-types";

describe("GovernancePaymaster", function () {
    let governanceToken: GovernanceToken;
    let paymaster: GovernancePaymaster;
    let owner: Signer;
    let user1: Signer;
    let user2: Signer;
    let user3: Signer;
    
    const INITIAL_SUPPLY = ethers.parseEther("1000000"); // 1M tokens
    const MIN_VOTING_POWER = ethers.parseEther("1"); // 1 token minimum
    const INITIAL_ETH_FUNDING = ethers.parseEther("1"); // 1 ETH for paymaster
    
    beforeEach(async function () {
        [owner, user1, user2, user3] = await ethers.getSigners();
        
        // Deploy GovernanceToken first
        const GovernanceTokenFactory = await ethers.getContractFactory("GovernanceToken");
        governanceToken = await GovernanceTokenFactory.deploy(
            "Test Governance Token",
            "TGT"
        );
        await governanceToken.waitForDeployment();
        
        // Deploy GovernancePaymaster
        const PaymasterFactory = await ethers.getContractFactory("GovernancePaymaster");
        paymaster = await PaymasterFactory.deploy(await governanceToken.getAddress());
        await paymaster.waitForDeployment();
        
        // Mint some tokens to users
        await governanceToken.mintForExchange(
            await user1.getAddress(),
            ethers.parseEther("100"),
            "Initial mint for testing"
        );
        
        await governanceToken.mintForExchange(
            await user2.getAddress(),
            ethers.parseEther("50"),
            "Initial mint for testing"
        );
        
        // user3 gets no tokens (for negative testing)
        
        // Delegate voting power to themselves
        await governanceToken.connect(user1).delegateVoting(await user1.getAddress());
        await governanceToken.connect(user2).delegateVoting(await user2.getAddress());
        
        // Fund the paymaster with ETH
        await owner.sendTransaction({
            to: await paymaster.getAddress(),
            value: INITIAL_ETH_FUNDING
        });
    });

    describe("Deployment", function () {
        it("Should set the correct governance token address", async function () {
            expect(await paymaster.governanceToken()).to.equal(await governanceToken.getAddress());
        });

        it("Should set the correct owner", async function () {
            expect(await paymaster.owner()).to.equal(await owner.getAddress());
        });

        it("Should have default parameters set correctly", async function () {
            const [maxGasPrice, maxGasLimit, minVotingPower] = await paymaster.getParameters();
            
            expect(maxGasPrice).to.equal(50n * 10n**9n); // 50 Gwei
            expect(maxGasLimit).to.equal(1_000_000n);
            expect(minVotingPower).to.equal(ethers.parseEther("1"));
        });

        it("Should not be paused initially", async function () {
            const [,,,isPaused] = await paymaster.getStats();
            expect(isPaused).to.be.false;
        });

        it("Should receive ETH funding", async function () {
            const balance = await paymaster.getBalance();
            expect(balance).to.equal(INITIAL_ETH_FUNDING);
        });
    });

    describe("Governance Function Validation", function () {
        it("Should allow valid governance function selectors", async function () {
            const voteSelector = ethers.id("vote(uint256,bool)").slice(0, 10);
            const proposalSelector = ethers.id("createProposal(string,uint256)").slice(0, 10);
            const delegateSelector = ethers.id("delegateVoting(address)").slice(0, 10);
            const standardDelegateSelector = ethers.id("delegate(address)").slice(0, 10);
            
            expect(await paymaster.isAllowedGovernanceFunction(voteSelector)).to.be.true;
            expect(await paymaster.isAllowedGovernanceFunction(proposalSelector)).to.be.true;
            expect(await paymaster.isAllowedGovernanceFunction(delegateSelector)).to.be.true;
            expect(await paymaster.isAllowedGovernanceFunction(standardDelegateSelector)).to.be.true;
        });

        it("Should reject invalid function selectors", async function () {
            const transferSelector = ethers.id("transfer(address,uint256)").slice(0, 10);
            const invalidSelector = "0x12345678";
            
            expect(await paymaster.isAllowedGovernanceFunction(transferSelector)).to.be.false;
            expect(await paymaster.isAllowedGovernanceFunction(invalidSelector)).to.be.false;
        });

        it("Should provide function support check", async function () {
            const voteSelector = ethers.id("vote(uint256,bool)").slice(0, 10);
            expect(await paymaster.isFunctionSupported(voteSelector)).to.be.true;
        });
    });

    describe("Eligibility Checks", function () {
        it("Should approve eligible users with sufficient voting power", async function () {
            const user1Addr = await user1.getAddress();
            expect(await paymaster.isEligible(user1Addr)).to.be.true;
        });

        it("Should approve users with minimum voting power", async function () {
            const user2Addr = await user2.getAddress();
            expect(await paymaster.isEligible(user2Addr)).to.be.true;
        });

        it("Should reject users with insufficient voting power", async function () {
            const user3Addr = await user3.getAddress();
            expect(await paymaster.isEligible(user3Addr)).to.be.false;
        });

        it("Should reject blocked users even with voting power", async function () {
            const user1Addr = await user1.getAddress();
            
            // Initially eligible
            expect(await paymaster.isEligible(user1Addr)).to.be.true;
            
            // Block user
            await paymaster.blockUser(user1Addr);
            expect(await paymaster.isEligible(user1Addr)).to.be.false;
            
            // Unblock user
            await paymaster.unblockUser(user1Addr);
            expect(await paymaster.isEligible(user1Addr)).to.be.true;
        });

        it("Should reject all users when paused", async function () {
            const user1Addr = await user1.getAddress();
            
            // Initially eligible
            expect(await paymaster.isEligible(user1Addr)).to.be.true;
            
            // Pause paymaster
            await paymaster.pause();
            expect(await paymaster.isEligible(user1Addr)).to.be.false;
            
            // Unpause
            await paymaster.unpause();
            expect(await paymaster.isEligible(user1Addr)).to.be.true;
        });
    });

    describe("Admin Functions", function () {
        it("Should allow owner to pause and unpause", async function () {
            await paymaster.pause();
            let [,,,isPaused] = await paymaster.getStats();
            expect(isPaused).to.be.true;
            
            await paymaster.unpause();
            [,,,isPaused] = await paymaster.getStats();
            expect(isPaused).to.be.false;
        });

        it("Should emit events when pausing/unpausing", async function () {
            await expect(paymaster.pause())
                .to.emit(paymaster, "PaymasterPaused")
                .withArgs(true);
                
            await expect(paymaster.unpause())
                .to.emit(paymaster, "PaymasterPaused")
                .withArgs(false);
        });

        it("Should allow owner to update parameters", async function () {
            const newGasPrice = 75n * 10n**9n; // 75 Gwei
            const newGasLimit = 1_500_000n;
            const newMinVoting = ethers.parseEther("5");
            
            await expect(paymaster.updateParameters(newGasPrice, newGasLimit, newMinVoting))
                .to.emit(paymaster, "ParametersUpdated")
                .withArgs(newGasPrice, newGasLimit, newMinVoting);
                
            const [maxGasPrice, maxGasLimit, minVotingPower] = await paymaster.getParameters();
            expect(maxGasPrice).to.equal(newGasPrice);
            expect(maxGasLimit).to.equal(newGasLimit);
            expect(minVotingPower).to.equal(newMinVoting);
        });

        it("Should allow owner to update minimum voting power separately", async function () {
            const newMinVoting = ethers.parseEther("10");
            
            await expect(paymaster.updateMinVotingPower(newMinVoting))
                .to.emit(paymaster, "VotingPowerRequirementUpdated")
                .withArgs(MIN_VOTING_POWER, newMinVoting);
                
            const [,,minVotingPower] = await paymaster.getParameters();
            expect(minVotingPower).to.equal(newMinVoting);
        });

        it("Should allow owner to block and unblock users", async function () {
            const user1Addr = await user1.getAddress();
            
            await expect(paymaster.blockUser(user1Addr))
                .to.emit(paymaster, "UserBlocked")
                .withArgs(user1Addr);
                
            expect(await paymaster.blockedUsers(user1Addr)).to.be.true;
            
            await expect(paymaster.unblockUser(user1Addr))
                .to.emit(paymaster, "UserUnblocked")
                .withArgs(user1Addr);
                
            expect(await paymaster.blockedUsers(user1Addr)).to.be.false;
        });

        it("Should allow owner to withdraw funds", async function () {
            const initialBalance = await ethers.provider.getBalance(await paymaster.getAddress());
            const recipient = await user1.getAddress();
            
            await expect(paymaster.withdraw(recipient))
                .to.emit(paymaster, "FundsWithdrawn")
                .withArgs(recipient, initialBalance);
                
            expect(await paymaster.getBalance()).to.equal(0);
        });

        it("Should reject non-owner admin operations", async function () {
            await expect(paymaster.connect(user1).pause())
                .to.be.revertedWithCustomError(paymaster, "OwnableUnauthorizedAccount");
                
            await expect(paymaster.connect(user1).updateParameters(1, 1, 1))
                .to.be.revertedWithCustomError(paymaster, "OwnableUnauthorizedAccount");
                
            await expect(paymaster.connect(user1).blockUser(await user2.getAddress()))
                .to.be.revertedWithCustomError(paymaster, "OwnableUnauthorizedAccount");
                
            await expect(paymaster.connect(user1).withdraw(await user1.getAddress()))
                .to.be.revertedWithCustomError(paymaster, "OwnableUnauthorizedAccount");
        });

        it("Should reject invalid parameter updates", async function () {
            await expect(paymaster.updateParameters(0, 1000000, ethers.parseEther("1")))
                .to.be.revertedWith("Gas price must be > 0");
                
            await expect(paymaster.updateParameters(50000000000, 0, ethers.parseEther("1")))
                .to.be.revertedWith("Gas limit must be > 0");
                
            await expect(paymaster.updateParameters(50000000000, 1000000, 0))
                .to.be.revertedWith("Min voting power must be > 0");
                
            await expect(paymaster.updateMinVotingPower(0))
                .to.be.revertedWith("Min voting power must be > 0");
        });

        it("Should reject blocking/unblocking zero address", async function () {
            await expect(paymaster.blockUser(ethers.ZeroAddress))
                .to.be.revertedWith("Invalid user address");
                
            await expect(paymaster.unblockUser(ethers.ZeroAddress))
                .to.be.revertedWith("Invalid user address");
        });

        it("Should reject withdrawal to zero address", async function () {
            await expect(paymaster.withdraw(ethers.ZeroAddress))
                .to.be.revertedWith("Invalid recipient");
        });

        it("Should reject withdrawal when no funds", async function () {
            // Withdraw all funds first
            await paymaster.withdraw(await user1.getAddress());
            
            await expect(paymaster.withdraw(await user1.getAddress()))
                .to.be.revertedWith("No funds to withdraw");
        });
    });

    describe("Statistics and Views", function () {
        it("Should return correct initial stats", async function () {
            const [totalTx, totalGas, balance, isPaused] = await paymaster.getStats();
            
            expect(totalTx).to.equal(0);
            expect(totalGas).to.equal(0);
            expect(balance).to.equal(INITIAL_ETH_FUNDING);
            expect(isPaused).to.be.false;
        });

        it("Should return correct user stats initially", async function () {
            const [txCount, gassPaid] = await paymaster.getUserStats(await user1.getAddress());
            
            expect(txCount).to.equal(0);
            expect(gassPaid).to.equal(0);
        });

        it("Should return correct parameters", async function () {
            const [gasPrice, gasLimit, minVoting] = await paymaster.getParameters();
            
            expect(gasPrice).to.equal(50n * 10n**9n);
            expect(gasLimit).to.equal(1_000_000n);
            expect(minVoting).to.equal(ethers.parseEther("1"));
        });

        it("Should return correct balance", async function () {
            expect(await paymaster.getBalance()).to.equal(INITIAL_ETH_FUNDING);
        });
    });

    describe("Constructor Validation", function () {
        it("Should reject zero address for governance token", async function () {
            const PaymasterFactory = await ethers.getContractFactory("GovernancePaymaster");
            
            await expect(PaymasterFactory.deploy(ethers.ZeroAddress))
                .to.be.revertedWith("Invalid governance token");
        });
    });

    describe("Receive Function", function () {
        it("Should accept ETH transfers", async function () {
            const additionalFunding = ethers.parseEther("0.5");
            const initialBalance = await paymaster.getBalance();
            
            await owner.sendTransaction({
                to: await paymaster.getAddress(),
                value: additionalFunding
            });
            
            expect(await paymaster.getBalance()).to.equal(initialBalance + additionalFunding);
        });
    });

    describe("Integration with GovernanceToken", function () {
        it("Should correctly check voting power from governance token", async function () {
            const user1Addr = await user1.getAddress();
            const user1VotingPower = await governanceToken.getVotes(user1Addr);
            
            // User1 should have 100 tokens worth of voting power
            expect(user1VotingPower).to.equal(ethers.parseEther("100"));
            expect(await paymaster.isEligible(user1Addr)).to.be.true;
        });

        it("Should update eligibility when voting power changes", async function () {
            const user3Addr = await user3.getAddress();
            
            // Initially not eligible
            expect(await paymaster.isEligible(user3Addr)).to.be.false;
            
            // Give user3 some tokens
            await governanceToken.mintForExchange(
                user3Addr,
                ethers.parseEther("5"),
                "Testing eligibility"
            );
            
            // Still not eligible until delegated
            expect(await paymaster.isEligible(user3Addr)).to.be.false;
            
            // Delegate to self
            await governanceToken.connect(user3).delegateVoting(user3Addr);
            
            // Now eligible
            expect(await paymaster.isEligible(user3Addr)).to.be.true;
        });
    });

    // Note: The actual paymaster validation function would require bootloader context
    // which is not available in standard test environment. These tests focus on
    // the business logic and validation that can be tested in isolation.
    
    describe("Gas Estimation Helper", function () {
        it("Should provide gas estimation for typical operations", async function () {
            // This is a utility test to help with frontend integration
            const voteCalldata = governanceToken.interface.encodeFunctionData("vote", [1, true]);
            const proposalCalldata = governanceToken.interface.encodeFunctionData("createProposal", [
                "Test proposal",
                Math.floor(Date.now() / 1000) + 3600 // 1 hour from now
            ]);
            
            expect(voteCalldata.length).to.be.greaterThan(4);
            expect(proposalCalldata.length).to.be.greaterThan(4);
            
            // Verify selectors match what paymaster expects
            const voteSelector = voteCalldata.slice(0, 10);
            const proposalSelector = proposalCalldata.slice(0, 10);
            
            expect(await paymaster.isAllowedGovernanceFunction(voteSelector)).to.be.true;
            expect(await paymaster.isAllowedGovernanceFunction(proposalSelector)).to.be.true;
        });
    });
});