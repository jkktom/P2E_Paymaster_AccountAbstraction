# Deployment Strategy - Jenkins + Docker on Single AWS Instance

This file documents the deployment architecture using Jenkins CI/CD and Docker containers on a single AWS Ubuntu instance.

## Deployment Architecture

```
GitHub → Single AWS Instance (Ubuntu t3.large) → Frontend (Vercel)
   ↓              ↓                                    ↓
Webhook    Jenkins CI/CD                         Auto Deploy
           Docker Containers                      (on git push)
           (Jenkins + Backend + PostgreSQL)
```

## Infrastructure Overview

### Single AWS Instance (t3.large - All-in-One)
- **OS**: Ubuntu 22.04 LTS
- **Resources**: 2 vCPUs, 8GB RAM, 30GB SSD
- **Services**: Jenkins, Spring Boot Backend, PostgreSQL Database
- **Cost**: ~$60/month (50% cost savings vs separate instances)

### Frontend (Vercel)
- **Purpose**: Static site hosting with edge optimization
- **Deployment**: Automatic deployment on git push to main branch

## AWS Ubuntu Instance Setup

### Initial Server Setup
```bash
#!/bin/bash
# Single instance setup script for Ubuntu 22.04

# Update system
sudo apt update && sudo apt upgrade -y

# Install essential packages
sudo apt install -y curl wget git unzip

# Install Java 17 (for Jenkins and Spring Boot)
sudo apt install -y openjdk-17-jdk

# Verify Java installation
java -version

# Install Docker
sudo apt install -y docker.io
sudo systemctl start docker
sudo systemctl enable docker

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Install Jenkins
wget -q -O - https://pkg.jenkins.io/debian-stable/jenkins.io.key | sudo apt-key add -
echo "deb https://pkg.jenkins.io/debian-stable binary/" | sudo tee -a /etc/apt/sources.list
sudo apt update
sudo apt install -y jenkins

# Configure user permissions
sudo usermod -a -G docker jenkins
sudo usermod -a -G docker ubuntu

# Install Node.js (for any frontend build tasks)
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# Install Gradle
sudo apt install -y gradle

# Start Jenkins
sudo systemctl start jenkins
sudo systemctl enable jenkins

# Verify AWS CLI (pre-installed on Ubuntu AMI)
aws --version

# Create application directory
mkdir -p /home/ubuntu/blockchain-exchange
sudo chown ubuntu:ubuntu /home/ubuntu/blockchain-exchange

echo "Setup complete!"
echo "Jenkins URL: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):8080"
echo "Initial Jenkins password: $(sudo cat /var/lib/jenkins/secrets/initialAdminPassword)"
```

### Security Group Configuration
```bash
# Inbound Rules:
# SSH (22) - Your IP only
# HTTP (80) - 0.0.0.0/0
# HTTPS (443) - 0.0.0.0/0  
# Jenkins (8080) - Your IP only
# Backend API (8081) - 0.0.0.0/0

# Outbound Rules:
# All traffic - 0.0.0.0/0
```

## Docker Configuration

### Production Docker Compose
```yaml
# /home/ubuntu/blockchain-exchange/docker-compose.yml
services:
  jenkins:
    image: jenkins/jenkins:lts
    container_name: jenkins
    ports:
      - "8080:8080"
      - "50000:50000"
    volumes:
      - jenkins_data:/var/jenkins_home
      - /var/run/docker.sock:/var/run/docker.sock:ro
      - /usr/local/bin/docker-compose:/usr/local/bin/docker-compose:ro
      - /usr/bin/docker:/usr/bin/docker:ro
    environment:
      - JENKINS_OPTS=--httpPort=8080
    user: root
    restart: unless-stopped
    networks:
      - app-network
    
  backend:
    image: ${ECR_REGISTRY}/blockchain-exchange-backend:latest
    container_name: backend
    ports:
      - "8081:8080"  # External port 8081 to avoid Jenkins conflict
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - DATABASE_URL=jdbc:postgresql://postgres:5432/exchangedb
      - DATABASE_PASSWORD=${DATABASE_PASSWORD}
      - ALCHEMY_API_KEY=${ALCHEMY_API_KEY}
      - ALCHEMY_APP_ID=${ALCHEMY_APP_ID}
      - GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
      - GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}
      - JWT_SECRET=${JWT_SECRET}
      - ALLOWED_ORIGINS=${ALLOWED_ORIGINS}
    depends_on:
      postgres:
        condition: service_healthy
    restart: unless-stopped
    networks:
      - app-network
    
  postgres:
    image: postgres:15
    container_name: postgres
    environment:
      POSTGRES_DB: exchangedb
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DATABASE_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped
    networks:
      - app-network

  nginx:
    image: nginx:alpine
    container_name: nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl-certs:/etc/ssl/certs
    depends_on:
      - backend
    restart: unless-stopped
    networks:
      - app-network

volumes:
  jenkins_data:
  postgres_data:

networks:
  app-network:
    driver: bridge
```

### Environment Configuration
```bash
# /home/ubuntu/blockchain-exchange/.env
ECR_REGISTRY=your-account-id.dkr.ecr.us-east-1.amazonaws.com
DATABASE_PASSWORD=your-secure-database-password
ALCHEMY_API_KEY=sk_test_your_alchemy_key
ALCHEMY_APP_ID=your-alchemy-app-id
GOOGLE_CLIENT_ID=your-google-client-id.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret
JWT_SECRET=your-jwt-secret-key
ALLOWED_ORIGINS=https://your-frontend-domain.vercel.app
```

### Backend Dockerfile (Gradle)
```dockerfile
# backend/Dockerfile
FROM openjdk:17-jdk-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew build.gradle settings.gradle ./
COPY gradle gradle

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src ./src

# Build application
RUN ./gradlew bootJar --no-daemon

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
CMD ["java", "-jar", "build/libs/blockchain-exchange-backend-0.0.1-SNAPSHOT.jar"]
```

## Jenkins Configuration

### Jenkins Initial Setup
```bash
# Access Jenkins at http://your-server-ip:8080
# Get initial password:
sudo cat /var/lib/jenkins/secrets/initialAdminPassword

# Install suggested plugins plus:
# - Pipeline
# - Docker Pipeline  
# - Git
# - Gradle Plugin
# - NodeJS Plugin
# - AWS Steps (if using ECR)
```

### Jenkins Credentials Setup
```bash
# Add these credentials in Jenkins UI:
# Manage Jenkins → Manage Credentials → Global

# AWS ECR Registry URL
Kind: Secret text
ID: ecr-registry-url
Secret: your-account-id.dkr.ecr.us-east-1.amazonaws.com

# Database Password
Kind: Secret text
ID: database-password
Secret: your-secure-database-password

# Alchemy API Key
Kind: Secret text
ID: alchemy-api-key
Secret: sk_test_your_alchemy_key

# Google Client Secret
Kind: Secret text
ID: google-client-secret
Secret: your-google-oauth-client-secret

# JWT Secret
Kind: Secret text
ID: jwt-secret
Secret: your-jwt-secret-key

# Vercel Token (for frontend deployment)
Kind: Secret text
ID: vercel-token
Secret: your-vercel-token
```

### Complete Jenkinsfile
```groovy
pipeline {
    agent any
    
    tools {
        gradle 'Gradle-8'  // Configure in Global Tool Configuration
        nodejs 'NodeJS-18' // Configure in Global Tool Configuration
    }
    
    environment {
        ECR_REPOSITORY = 'blockchain-exchange-backend'
        ECR_REGISTRY = credentials('ecr-registry-url')
        AWS_DEFAULT_REGION = 'us-east-1'
        BUILD_NUMBER = "${env.BUILD_NUMBER}"
        COMPOSE_PROJECT_NAME = 'blockchain-exchange'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }
        
        stage('Environment Check') {
            steps {
                sh '''
                    echo "=== Environment Information ==="
                    java -version
                    gradle --version
                    node --version
                    docker --version
                    docker-compose --version
                    aws --version
                    echo "=============================="
                '''
            }
        }
        
        stage('Backend Tests') {
            steps {
                echo 'Running backend tests...'
                dir('backend') {
                    sh '''
                        chmod +x gradlew
                        ./gradlew clean test
                    '''
                }
            }
            post {
                always {
                    // Publish test results
                    publishTestResults testResultsPattern: 'backend/build/test-results/test/*.xml'
                }
            }
        }
        
        stage('Frontend Tests') {
            steps {
                echo 'Running frontend tests...'
                dir('frontend') {
                    sh '''
                        npm ci
                        npm run test -- --coverage --watchAll=false
                        npm run build
                    '''
                }
            }
        }
        
        stage('Smart Contract Tests') {
            steps {
                echo 'Running smart contract tests...'
                dir('contracts') {
                    sh '''
                        npm ci
                        npx hardhat test
                    '''
                }
            }
        }
        
        stage('Build Backend') {
            steps {
                echo 'Building backend application...'
                dir('backend') {
                    sh '''
                        ./gradlew bootJar
                        ls -la build/libs/
                    '''
                }
            }
        }
        
        stage('Build Docker Image') {
            when {
                branch 'main'
            }
            steps {
                echo 'Building Docker image...'
                script {
                    dir('backend') {
                        // Build Docker image locally
                        def image = docker.build("${ECR_REPOSITORY}:${BUILD_NUMBER}")
                        
                        // Tag for ECR
                        sh "docker tag ${ECR_REPOSITORY}:${BUILD_NUMBER} ${ECR_REGISTRY}/${ECR_REPOSITORY}:${BUILD_NUMBER}"
                        sh "docker tag ${ECR_REPOSITORY}:${BUILD_NUMBER} ${ECR_REGISTRY}/${ECR_REPOSITORY}:latest"
                        
                        // Login to ECR and push (AWS CLI is pre-installed)
                        sh '''
                            aws ecr get-login-password --region $AWS_DEFAULT_REGION | docker login --username AWS --password-stdin $ECR_REGISTRY
                            docker push $ECR_REGISTRY/$ECR_REPOSITORY:$BUILD_NUMBER
                            docker push $ECR_REGISTRY/$ECR_REPOSITORY:latest
                        '''
                        
                        echo "Docker image built and pushed successfully!"
                    }
                }
            }
        }
        
        stage('Deploy Backend') {
            when {
                branch 'main'
            }
            steps {
                echo 'Deploying backend locally...'
                script {
                    // Deploy on the same instance (no SSH needed)
                    sh '''
                        cd /home/ubuntu/blockchain-exchange
                        
                        # Login to ECR
                        aws ecr get-login-password --region $AWS_DEFAULT_REGION | docker login --username AWS --password-stdin $ECR_REGISTRY
                        
                        # Pull latest image
                        docker-compose pull backend
                        
                        # Restart backend service with zero-downtime
                        docker-compose up -d backend
                        
                        # Wait for service to be ready
                        echo "Waiting for backend to start..."
                        sleep 30
                        
                        # Health check
                        curl -f http://localhost:8081/actuator/health || exit 1
                        echo "Backend deployment successful!"
                    '''
                }
            }
        }
        
        stage('Deploy Frontend to Vercel') {
            when {
                branch 'main'
            }
            steps {
                echo 'Deploying frontend to Vercel...'
                dir('frontend') {
                    withCredentials([string(credentialsId: 'vercel-token', variable: 'VERCEL_TOKEN')]) {
                        sh '''
                            # Install Vercel CLI if not present
                            if ! command -v vercel &> /dev/null; then
                                npm install -g vercel
                            fi
                            
                            # Deploy to production
                            vercel --token $VERCEL_TOKEN --prod --yes
                            echo "Frontend deployment successful!"
                        '''
                    }
                }
            }
        }
        
        stage('Integration Tests') {
            when {
                branch 'main'
            }
            steps {
                echo 'Running integration tests...'
                script {
                    // Wait for services to stabilize
                    sleep 60
                    
                    sh '''
                        # Test backend API endpoints
                        curl -f http://localhost:8081/actuator/health
                        curl -f http://localhost:8081/api/auth/google || echo "Auth endpoint needs configuration"
                        
                        # Test database connectivity
                        docker exec postgres pg_isready -U postgres
                        
                        echo "Integration tests completed!"
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline completed'
            
            // Clean up workspace
            cleanWs()
            
            // Clean up unused Docker images to save space
            sh 'docker image prune -f'
        }
        
        success {
            echo 'Pipeline succeeded! 🎉'
            // Future: Add Slack/email notifications
        }
        
        failure {
            echo 'Pipeline failed! ❌'
            // Future: Add failure notifications
        }
    }
}
```

## Nginx Configuration

### Nginx Reverse Proxy
```nginx
# /home/ubuntu/blockchain-exchange/nginx.conf
events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    
    upstream backend {
        server backend:8080;
    }
    
    upstream jenkins {
        server jenkins:8080;
    }

    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    limit_req_zone $binary_remote_addr zone=jenkins:10m rate=5r/s;

    # Main application server
    server {
        listen 80;
        server_name your-domain.com www.your-domain.com;

        # Security headers
        add_header X-Frame-Options DENY;
        add_header X-Content-Type-Options nosniff;
        add_header X-XSS-Protection "1; mode=block";
        add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

        # API endpoints
        location /api/ {
            limit_req zone=api burst=20 nodelay;
            
            proxy_pass http://backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # Timeout settings
            proxy_connect_timeout 60s;
            proxy_send_timeout 60s;
            proxy_read_timeout 60s;
        }

        # Health check endpoint
        location /actuator/health {
            proxy_pass http://backend/actuator/health;
            access_log off;
        }
    }

    # Jenkins server (separate subdomain or port)
    server {
        listen 80;
        server_name jenkins.your-domain.com;

        location / {
            limit_req zone=jenkins burst=10 nodelay;
            
            proxy_pass http://jenkins;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # Jenkins-specific headers
            proxy_set_header X-Forwarded-Port $server_port;
        }
    }
}
```

## Deployment Commands

### Initial Deployment
```bash
# Clone repository
cd /home/ubuntu
git clone https://github.com/your-username/blockchain-exchange.git
cd blockchain-exchange

# Set up environment
cp .env.example .env
# Edit .env with your values

# Start all services
docker-compose up -d

# Check status
docker-compose ps
docker-compose logs -f
```

### Daily Operations
```bash
# Check service status
docker-compose ps

# View logs
docker-compose logs -f backend
docker-compose logs -f jenkins
docker-compose logs -f postgres

# Restart services
docker-compose restart backend
docker-compose restart jenkins

# Update application (handled by Jenkins, but manual option)
docker-compose pull
docker-compose up -d

# Database backup
docker exec postgres pg_dump -U postgres exchangedb > backup_$(date +%Y%m%d).sql

# Clean up old images
docker image prune -f

# Monitor resource usage
docker stats
htop
```

## Monitoring and Maintenance

### Health Checks
```bash
# Application health
curl http://localhost:8081/actuator/health

# Jenkins health
curl http://localhost:8080/login

# Database health
docker exec postgres pg_isready -U postgres

# System resources
free -h
df -h
docker system df
```

### Backup Strategy
```bash
# Automated backup script (/home/ubuntu/backup.sh)
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/home/ubuntu/backups"

mkdir -p $BACKUP_DIR

# Database backup
docker exec postgres pg_dump -U postgres exchangedb > $BACKUP_DIR/db_backup_$DATE.sql

# Jenkins backup
docker exec jenkins tar -czf - -C /var/jenkins_home . > $BACKUP_DIR/jenkins_backup_$DATE.tar.gz

# Keep only last 7 days of backups
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete
find $BACKUP_DIR -name "*.tar.gz" -mtime +7 -delete

echo "Backup completed: $DATE"

# Add to crontab: 0 2 * * * /home/ubuntu/backup.sh
```

## Security Configuration

### Firewall Setup
```bash
# Configure Ubuntu UFW firewall
sudo ufw enable
sudo ufw default deny incoming
sudo ufw default allow outgoing

# Allow SSH (your IP only)
sudo ufw allow from YOUR_IP_ADDRESS to any port 22

# Allow HTTP/HTTPS
sudo ufw allow 80
sudo ufw allow 443

# Allow Jenkins (your IP only)
sudo ufw allow from YOUR_IP_ADDRESS to any port 8080

# Allow backend API
sudo ufw allow 8081

sudo ufw status verbose
```

### SSL Configuration (Optional)
```bash
# Install Let's Encrypt
sudo apt install -y certbot python3-certbot-nginx

# Get SSL certificate
sudo certbot --nginx -d your-domain.com -d www.your-domain.com

# Auto-renewal
echo "0 12 * * * /usr/bin/certbot renew --quiet" | sudo crontab -
```

## Cost Analysis

### Monthly Infrastructure Costs
- **Single t3.large Instance**: ~$60/month
- **30GB EBS Storage**: ~$3/month
- **Data Transfer**: ~$5/month  
- **Elastic IP**: ~$3.65/month
- **ECR Storage**: ~$2/month
- **Total**: ~$74/month

### Cost Benefits
- **50% savings** vs separate Jenkins + Production instances
- **No GitHub Actions costs** (unlimited builds)
- **Simple management** (single server)
- **Perfect for POC/Demo** scenarios

## Interview Demonstration Points

### Technical Expertise Demonstrated
✅ **Single Instance Optimization**: Cost-effective resource utilization  
✅ **Jenkins CI/CD**: Complete pipeline automation  
✅ **Docker Orchestration**: Multi-container application management  
✅ **AWS Integration**: Native AWS CLI usage and ECR integration  
✅ **Security Practices**: Proper firewall and credential management  
✅ **Monitoring Setup**: Health checks and logging strategies  
✅ **Backup Strategy**: Data persistence and recovery planning  
✅ **Production Ready**: Real-world deployment considerations

This single-instance approach demonstrates practical DevOps skills while maintaining cost efficiency and showing understanding of resource optimization - perfect for technical interview scenarios.