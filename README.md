# strawberry-irrigation-system
🍓 基于Spring Boot + React的智能农业灌溉系统，集成IoT传感器监测、MQTT通信、TimescaleDB时序数据处理与自动化控制
# 🍓 草莓栽培智能灌溉系统

<div align="center">

![Logo](screenshots/logo.png)

**基于物联网和人工智能的现代农业解决方案**

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen?style=flat-square)]()
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=java)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7-6DB33F?style=flat-square&logo=springboot)]()
[![React](https://img.shields.io/badge/React-18-61DAFB?style=flat-square&logo=react)]()
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14-336791?style=flat-square&logo=postgresql)]()
[![Docker](https://img.shields.io/badge/Docker-20.10-2496ED?style=flat-square&logo=docker)]()
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)]()

[📱 在线演示](https://strawberry-irrigation.demo.com) • [📖 完整文档](./docs/) • [🐛 问题反馈](https://github.com/yourusername/strawberry-irrigation-system/issues)

</div>

---

## 🌱 项目简介

智能灌溉系统是一个基于物联网技术的现代农业解决方案，专门为草莓栽培设计。系统通过传感器实时监测土壤湿度、温度、光照等环境参数，结合智能规则引擎实现自动化灌溉控制，旨在**提高农业生产效率、节约水资源、降低人工成本**。

### ✨ 核心特性

<table>
<tr>
<td width="50%">

#### 🌡️ **实时环境监测**
- 土壤湿度精确测量
- 温度变化实时跟踪  
- 光照强度智能感知
- 设备电量状态监控

#### 💧 **智能自动灌溉**
- 基于规则引擎的自动控制
- 多种触发条件支持（<, <=, >, >=）
- 灌溉时长精确控制
- 异常情况自动报警

</td>
<td width="50%">

#### 📱 **多端管理平台**
- Web管理后台（PC端）
- React Native移动应用
- 响应式界面设计
- 实时数据同步

#### 📊 **数据分析可视化**
- 历史数据趋势图表
- 设备运行状态统计
- 灌溉效率分析报告
- 自定义时间范围查询

</td>
</tr>
</table>

---

## 🏆 技术亮点

### 🚀 **架构设计**
- **模块化单体架构** - 高内聚、低耦合的模块设计
- **RESTful API设计** - 标准化的接口规范
- **JWT无状态认证** - 支持水平扩展的安全机制
- **Docker容器化部署** - 一键部署，环境一致性保障

### 📈 **性能优化**
- **时序数据库优化** - TimescaleDB处理海量传感器数据
- **多级缓存策略** - Redis缓存提升响应速度
- **批量数据处理** - 异步批处理机制
- **数据库索引优化** - 针对查询场景的索引设计

### 🔗 **物联网技术**
- **MQTT协议通信** - 低延迟、高可靠性设备通信
- **设备状态管理** - 在线状态实时监控
- **断线重连机制** - 网络异常自动恢复
- **消息持久化** - 确保数据不丢失

---

## 🎯 系统架构

<div align="center">

![系统架构图](docs/images/architecture-overview.png)

</div>

### 🛠️ 技术栈

<table>
<tr>
<td><b>后端框架</b></td>
<td>
<img src="https://img.shields.io/badge/Spring%20Boot-2.7-6DB33F?style=flat-square&logo=springboot" alt="Spring Boot">
<img src="https://img.shields.io/badge/Spring%20Security-5.7-6DB33F?style=flat-square&logo=springsecurity" alt="Spring Security">
<img src="https://img.shields.io/badge/Spring%20Data%20JPA-2.7-6DB33F?style=flat-square" alt="Spring Data JPA">
</td>
</tr>
<tr>
<td><b>前端框架</b></td>
<td>
<img src="https://img.shields.io/badge/React-18-61DAFB?style=flat-square&logo=react" alt="React">
<img src="https://img.shields.io/badge/Ant%20Design%20Pro-5.x-1890FF?style=flat-square&logo=antdesign" alt="Ant Design Pro">
<img src="https://img.shields.io/badge/React%20Native-0.70-61DAFB?style=flat-square&logo=react" alt="React Native">
</td>
</tr>
<tr>
<td><b>数据存储</b></td>
<td>
<img src="https://img.shields.io/badge/PostgreSQL-14-336791?style=flat-square&logo=postgresql" alt="PostgreSQL">
<img src="https://img.shields.io/badge/TimescaleDB-2.8-FDB515?style=flat-square" alt="TimescaleDB">
<img src="https://img.shields.io/badge/Redis-7.0-DC382D?style=flat-square&logo=redis" alt="Redis">
</td>
</tr>
<tr>
<td><b>消息队列</b></td>
<td>
<img src="https://img.shields.io/badge/EMQX-5.1-00D4AA?style=flat-square" alt="EMQX">
<img src="https://img.shields.io/badge/MQTT-3.1.1-660066?style=flat-square" alt="MQTT">
</td>
</tr>
<tr>
<td><b>容器化</b></td>
<td>
<img src="https://img.shields.io/badge/Docker-20.10-2496ED?style=flat-square&logo=docker" alt="Docker">
<img src="https://img.shields.io/badge/Docker%20Compose-2.0-2496ED?style=flat-square&logo=docker" alt="Docker Compose">
</td>
</tr>
</table>

---

## 🚀 快速开始

### 📋 环境要求

| 工具 | 版本要求 | 说明 |
|------|----------|------|
| Java | 17+ | 后端运行环境 |
| Node.js | 16+ | 前端开发环境 |
| Docker | 20.10+ | 容器化部署 |
| Docker Compose | 2.0+ | 服务编排 |

### ⚡ 一键启动

```bash
# 1. 克隆项目
git clone https://github.com/yourusername/strawberry-irrigation-system.git
cd strawberry-irrigation-system

# 2. 启动基础服务（数据库、缓存、消息队列）
docker-compose up -d

# 3. 启动后端服务
cd backend
./mvnw spring-boot:run

# 4. 启动前端服务
cd ../frontend
npm install
npm start
