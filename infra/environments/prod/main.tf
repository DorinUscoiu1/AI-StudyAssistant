// Minimal Terraform to create: VPC, public/private subnets, internet gateway, nat gateway, security groups, ECR, ECS cluster, ALB, RDS Postgres, Secrets Manager

data "aws_availability_zones" "available" {}

resource "aws_vpc" "main" {
  cidr_block = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags = {
    Name = "${var.project}-${var.environment}-vpc"
  }
}

resource "aws_subnet" "public" {
  count                   = 2
  vpc_id                  = aws_vpc.main.id
  cidr_block              = cidrsubnet(var.vpc_cidr, 8, count.index)
  map_public_ip_on_launch = true
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  tags                    = { Name = "${var.project}-${var.environment}-public-${count.index}" }
}

resource "aws_subnet" "private" {
  count                   = 2
  vpc_id                  = aws_vpc.main.id
  cidr_block              = cidrsubnet(var.vpc_cidr, 8, count.index + 10)
  map_public_ip_on_launch = false
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  tags                    = { Name = "${var.project}-${var.environment}-private-${count.index}" }
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.main.id
  tags   = { Name = "${var.project}-${var.environment}-igw" }
}

// ECR Repository
resource "aws_ecr_repository" "app" {
  name                 = "${var.project}-repo"
  image_tag_mutability = "MUTABLE"
}

// ECS Cluster
resource "aws_ecs_cluster" "cluster" {
  name = "${var.project}-${var.environment}-cluster"
}

// Security group for ALB
resource "aws_security_group" "alb_sg" {
  name        = "${var.project}-${var.environment}-alb-sg"
  description = "Allow HTTP/HTTPS"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

// Security group for ECS tasks
resource "aws_security_group" "ecs_sg" {
  name        = "${var.project}-${var.environment}-ecs-sg"
  description = "Allow traffic from ALB only"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 9090
    to_port         = 9090
    protocol        = "tcp"
    security_groups = [aws_security_group.alb_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

// Security group for RDS
resource "aws_security_group" "rds_sg" {
  name        = "${var.project}-${var.environment}-rds-sg"
  description = "Allow Postgres from ECS"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 5432
    to_port         = 5432
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

// RDS subnet group
resource "aws_db_subnet_group" "default" {
  name       = "${var.project}-${var.environment}-db-subnet-group"
  subnet_ids = aws_subnet.private[*].id
}

// RDS instance PostgreSQL
resource "aws_db_instance" "postgres" {
  allocated_storage       = 20
  engine                  = "postgres"
  engine_version          = "15"
  instance_class          = "db.t3.medium"
  db_name                 = "studydb"
  identifier              = "studydb"
  username                = var.db_username
  password                = random_password.db_password.result
  skip_final_snapshot     = true # Set to true to avoid issues when tearing down for school projects
  multi_az                = false # Set to false to reduce costs for school projects
  backup_retention_period = 7
  vpc_security_group_ids  = [aws_security_group.rds_sg.id]
  db_subnet_group_name    = aws_db_subnet_group.default.name
}

resource "random_password" "db_password" {
  length           = 16
  special          = true
  override_special = "#_-"
}

// Secrets Manager store DB password
resource "aws_secretsmanager_secret" "db" {
  name = "${var.project}-${var.environment}-db-secret-${random_string.suffix.result}"
}

resource "random_string" "suffix" {
  length  = 4
  special = false
}

resource "aws_secretsmanager_secret_version" "db_version" {
  secret_id     = aws_secretsmanager_secret.db.id
  secret_string = jsonencode({ username = var.db_username, password = random_password.db_password.result })
}

// CloudWatch log group for ECS
resource "aws_cloudwatch_log_group" "ecs" {
  name              = "/ecs/${var.project}"
  retention_in_days = 7
}

// Internet Gateway route table for public subnets
resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }
}

resource "aws_route_table_association" "public" {
  count          = length(aws_subnet.public)
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

// NAT Gateway for private subnets
resource "aws_eip" "nat" {
  domain = "vpc"
}

resource "aws_nat_gateway" "natgw" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.public[0].id
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.natgw.id
  }
}

resource "aws_route_table_association" "private" {
  count          = length(aws_subnet.private)
  subnet_id      = aws_subnet.private[count.index].id
  route_table_id = aws_route_table.private.id
}

// Application Load Balancer
resource "aws_lb" "alb" {
  name               = "${var.project}-${var.environment}-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb_sg.id]
  subnets            = aws_subnet.public[*].id
}

// Target Group (port 9090 to match application server.port)
resource "aws_lb_target_group" "tg" {
  name        = "${var.project}-${var.environment}-tg"
  port        = 9090
  protocol    = "HTTP"
  vpc_id      = aws_vpc.main.id
  target_type = "ip"

  health_check {
    path                = "/actuator/health"
    matcher             = "200"
    interval            = 15
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }
}

// ALB Listener
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.alb.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.tg.arn
  }
}

// IAM role for ECS Task Execution
resource "aws_iam_role" "ecs_task_execution" {
  name               = "${var.project}-${var.environment}-ecs-execution-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_task_assume_role.json
}

data "aws_iam_policy_document" "ecs_task_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_policy_attach" {
  role       = aws_iam_role.ecs_task_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

// Task role (for app permissions)
resource "aws_iam_role" "ecs_task_role" {
  name               = "${var.project}-${var.environment}-ecs-task-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_task_assume_role.json
}

// Policy for task role to read secrets and write logs
resource "aws_iam_policy" "ecs_task_policy" {
  name = "${var.project}-${var.environment}-ecs-task-policy"
  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = ["secretsmanager:GetSecretValue"],
        Resource = "*"
      },
      {
        Effect = "Allow",
        Action = ["logs:CreateLogStream", "logs:PutLogEvents"],
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_task_policy_attach" {
  role       = aws_iam_role.ecs_task_role.name
  policy_arn = aws_iam_policy.ecs_task_policy.arn
}

// ECS Task Definition
resource "aws_ecs_task_definition" "app" {
  family                   = "${var.project}-${var.environment}"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "512"
  memory                   = "1024"
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn

  container_definitions = jsonencode([
    {
      name      = "app"
      image     = "${aws_ecr_repository.app.repository_url}:${var.image_tag}"
      essential = true

      portMappings = [
        {
          containerPort = 9090
          hostPort      = 9090
          protocol      = "tcp"
        }
      ]

      environment = [
        {
          name  = "DB_URL",
          value = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/study"
        },
        {
          name  = "DB_USER",
          value = var.db_username
        }
      ]

      secrets = [
        {
          name      = "DB_PASSWORD",
          valueFrom = aws_secretsmanager_secret.db.arn
        }
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])
}

// ECS Service
resource "aws_ecs_service" "app_service" {
  name            = "${var.project}-${var.environment}-service"
  cluster         = aws_ecs_cluster.cluster.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = 1 # Keep at 1 to save costs for the school project
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_sg.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.tg.arn
    container_name   = "app"
    container_port   = 9090
  }

  depends_on = [aws_lb_listener.http]
}

// Outputs
output "ecr_repo" {
  value       = aws_ecr_repository.app.repository_url
  description = "ECR repository URL for pushing images"
}

output "rds_endpoint" {
  value       = aws_db_instance.postgres.address
  description = "RDS PostgreSQL endpoint"
}

output "alb_dns" {
  value       = aws_lb.alb.dns_name
  description = "ALB DNS name for accessing the application"
}

output "target_group_arn" {
  value       = aws_lb_target_group.tg.arn
  description = "ALB target group ARN"
}

output "db_secret_arn" {
  value       = aws_secretsmanager_secret.db.arn
  description = "Secrets Manager secret ARN for DB credentials"
}

