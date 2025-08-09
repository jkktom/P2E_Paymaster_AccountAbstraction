import { Provider, Wallet } from "zksync-ethers";
import { HardhatRuntimeEnvironment } from "hardhat/types";
import { Deployer } from "@matterlabs/hardhat-zksync-deploy";
import { ethers } from "ethers";

// 기존 GovernanceToken 주소 (이미 배포된 컨트랙트)
const GOVERNANCE_TOKEN_ADDRESS = "0x21341E1672ee0A7ddADB5D7BFF72F93C8E81EF3e"; // 배포된 GovernanceToken 주소 

export default async function deployPaymaster(hre: HardhatRuntimeEnvironment) {
  console.log(" Starting GovernancePaymaster deployment on zkSync Sepolia...");

  // Private key 확인
  const privateKey = process.env.PRIVATE_KEY;
  if (!privateKey) {
    throw new Error(" Private key not found in environment variables");
  }

  // zkSync provider 설정
  const provider = new Provider("https://sepolia.era.zksync.dev");
  
  // Wallet 생성
  const wallet = new Wallet(privateKey, provider);
  console.log(` Deploying from wallet: ${wallet.address}`);

  // 잔액 확인
  const balance = await wallet.getBalance();
  console.log(` Wallet balance: ${ethers.formatEther(balance.toString())} ETH`);

  if (balance < ethers.parseEther("0.01")) {
    throw new Error(" Insufficient balance. Need at least 0.01 ETH for deployment");
  }

  // Deployer 생성
  const deployer = new Deployer(hre, wallet);

  // GovernancePaymaster 컨트랙트 컴파일 및 로드
  console.log(" Compiling GovernancePaymaster...");
  const artifact = await deployer.loadArtifact("GovernancePaymaster");

  // Constructor 파라미터 준비
  const constructorArgs = [GOVERNANCE_TOKEN_ADDRESS];

  console.log(" Constructor arguments:");
  console.log(`  - Governance Token: ${GOVERNANCE_TOKEN_ADDRESS}`);

  try {
    // 배포 실행
    console.log(" Deploying contract...");
    const paymasterContract = await deployer.deploy(artifact, constructorArgs);

    // 배포 대기
    console.log("⏳ Waiting for deployment to be confirmed...");
    await paymasterContract.waitForDeployment();

    const contractAddress = await paymasterContract.getAddress();
    console.log(" GovernancePaymaster deployed successfully!");
    console.log(` Contract address: ${contractAddress}`);
    console.log(` zkSync Explorer: https://sepolia.era.zksync.dev/address/${contractAddress}`);

    // 배포 정보 저장
    const deploymentTx = await paymasterContract.deploymentTransaction();
    const deploymentInfo = {
      contractName: "GovernancePaymaster",
      contractAddress: contractAddress,
      governanceTokenAddress: GOVERNANCE_TOKEN_ADDRESS,
      network: "zkSync Sepolia",
      deployer: wallet.address,
      deploymentTime: new Date().toISOString(),
      txHash: deploymentTx?.hash || "unknown"
    };

    console.log("\n Deployment Summary:");
    console.log(JSON.stringify(deploymentInfo, null, 2));

    // 초기 설정 실행 (선택적)
    console.log("\n Performing initial setup...");
    
    // Paymaster에 초기 ETH 충전 (0.07 ETH)
    const fundAmount = ethers.parseEther("0.07");
    console.log(` Funding paymaster with ${ethers.formatEther(fundAmount)} ETH...`);
    
    const fundTx = await wallet.sendTransaction({
      to: contractAddress,
      value: fundAmount,
      gasLimit: 21000
    });
    
    await fundTx.wait();
    console.log(` Paymaster funded. TX: ${fundTx.hash}`);

    // 최종 상태 확인
    const paymasterBalance = await provider.getBalance(contractAddress);
    console.log(` Paymaster balance: ${ethers.formatEther(paymasterBalance.toString())} ETH`);

    console.log("\n Deployment and setup completed successfully!");
    
    return {
      paymasterAddress: contractAddress,
      governanceTokenAddress: GOVERNANCE_TOKEN_ADDRESS,
      deploymentTx: deploymentTx?.hash || "unknown"
    };

  } catch (error) {
    console.error("❌ Deployment failed:", error);
    throw error;
  }
}

// 스크립트가 직접 실행될 때
if (require.main === module) {
  const hre = require("hardhat");
  
  deployPaymaster(hre)
    .then((result) => {
      console.log("\n Script completed successfully");
      console.log(" Result:", result);
      process.exit(0);
    })
    .catch((error) => {
      console.error(" Script failed:", error);
      process.exit(1);
    });
}