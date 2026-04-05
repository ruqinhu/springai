#!/bin/bash
# validate.sh - Harness Engineering: 统一验证执行管道

set -e

echo "==========================================="
echo "⚙️  Starting Validation Pipeline (Harness)"
echo "==========================================="

echo "=> Step 1/3: Checking build & syntax..."
mvn clean compile -B -q
if [ $? -eq 0 ]; then
    echo "✅ Build Syntax Passed!"
else
    echo "❌ Build Failed! Fix compile errors first."
    exit 1
fi

echo "-------------------------------------------"
echo "=> Step 2/3: Checking Architectural Constraints (lint-arch)..."
mvn test -Dtest=ArchitectureConstraintsTest -B -q
if [ $? -eq 0 ]; then
    echo "✅ Architecture Lint Passed!"
else
    echo "❌ Architecture Lint Failed! Please review docs/ARCHITECTURE.md and trace the error."
    # 将失败记录存储到轨迹档案中（如果目录存在）
    if [ -d "harness/trace" ]; then
        echo "Auto-logged failure to harness/trace/arch_failure.log."
        mvn test -Dtest=ArchitectureConstraintsTest > harness/trace/arch_failure.log 2>&1 || true
    fi
    exit 1
fi

echo "-------------------------------------------"
echo "=> Step 3/3: Running Application Tests..."
# 排除了刚才已经跑过的架构测试
mvn test -Dexcludes=ArchitectureConstraintsTest -B -q
if [ $? -eq 0 ]; then
    echo "✅ Unit & Integration Tests Passed!"
else
    echo "❌ Tests Failed! Run 'mvn test' locally to inspect logs."
    exit 1
fi

echo "==========================================="
echo "🎉 All validations passed successfully! The Agent/Developer may proceed."
echo "==========================================="
exit 0
