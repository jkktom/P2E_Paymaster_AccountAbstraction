import { buildModule } from "@nomicfoundation/hardhat-ignition/modules";

const GovernanceTokenModule = buildModule("GovernanceTokenModule", (m) => {
  // Token configuration
  const tokenName = "Blooming Blockchain Service Token";
  const tokenSymbol = "BLOOM";

  // Deploy GovernanceToken contract
  const governanceToken = m.contract("GovernanceToken", [tokenName, tokenSymbol]);

  return { governanceToken };
});

export default GovernanceTokenModule;