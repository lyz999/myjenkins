package cn.lyz.myjenkins.entity;

import lombok.Data;

@Data
public class TaskEntity {
    String type;
    // 项目名
    String project;
    // Git仓库URL
    String gitRepoUrl;
    // 克隆到本地的目录
    String gitRepoDir;
    // Maven构建后的目标目录
    String targetDir;
    // WAR文件名
    String warFileName;
    // 最终部署的目标目录
    String destDir;
    // 用户名
    String username;
    // 密码
    String password;

}
