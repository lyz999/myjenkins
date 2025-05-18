package cn.lyz.myjenkins.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.lyz.myjenkins.entity.TaskEntity;
import cn.lyz.myjenkins.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {
    @Override
    public void task(TaskEntity taskEntity) {
        String type = taskEntity.getType();
        String GIT_REPO_URL = taskEntity.getGitRepoUrl();
        String GIT_REPO_DIR = taskEntity.getGitRepoDir();
        String USERNAME = taskEntity.getUsername();
        String PASSWORD = taskEntity.getPassword();
        String TARGET_DIR = taskEntity.getTargetDir();
        String WAR_FILE_NAME = taskEntity.getWarFileName();
        String DEST_DIR = taskEntity.getDestDir();
        log.info("开始执行任务 项目名：{}", taskEntity.getProject());
        // 克隆仓库（如果本地不存在）
        if (!new File(GIT_REPO_DIR).exists()) {
            log.info("本地Git仓库不存在，开始克隆");
            cloneRepository(USERNAME, PASSWORD, GIT_REPO_URL, GIT_REPO_DIR);
        }
        if ("java".equals(type)) {
            log.info("开始执行Java任务");
            try {
                if (checkForUpdates(USERNAME, PASSWORD, GIT_REPO_DIR)) {
                    log.info("检测到Git仓库更新，开始拉取最新代码");
                    pullLatestCode(USERNAME, PASSWORD, GIT_REPO_DIR);
                    log.info("检测到Git仓库更新，开始构建...");
                    buildAndDeploy(GIT_REPO_DIR, TARGET_DIR, WAR_FILE_NAME, DEST_DIR);
                } else {
                    log.info("没有检测到Git仓库更新");
                }
            } catch (Exception e) {
                log.error("发生异常", e);
            }
        } else if ("html".equals(type)) {
            log.info("开始执行Html任务");
            try {
                if (checkForUpdates(USERNAME, PASSWORD, GIT_REPO_DIR)) {
                    log.info("检测到Git仓库更新，开始拉取最新代码");
                    pullLatestCode(USERNAME, PASSWORD, GIT_REPO_DIR);
                    log.info("检测到Git仓库更新，开始复制");
                    //删除目标文件夹
                    FileUtil.del(DEST_DIR);
                    //复制文件
                    FileUtil.copyContent(new File(GIT_REPO_DIR), new File(DEST_DIR), true);
                    log.info("复制完成");
                    //设置权限
                    executeCommand(("chmod -R 777 " + DEST_DIR).split(" "), null);
                } else {
                    log.info("没有检测到Git仓库更新");
                }
            } catch (Exception e) {
                log.error("发生异常", e);
            }
        } else {
            log.info("不支持的任务类型");
        }
    }

    /*
     * 拉取 Git仓库
     */

    private void cloneRepository(String USERNAME, String PASSWORD, String GIT_REPO_URL, String GIT_REPO_DIR) {
        try {
            Git.cloneRepository().setCredentialsProvider(new UsernamePasswordCredentialsProvider(USERNAME, PASSWORD))
                    .setURI(GIT_REPO_URL)
                    .setDirectory(new File(GIT_REPO_DIR))
                    .call();
            log.info("Git仓库已克隆");
        } catch (Exception e) {
            log.error("克隆Git仓库时发生异常", e);
        }
    }

    private void pullLatestCode(String USERNAME, String PASSWORD, String GIT_REPO_DIR) {
        try {
            Git.open(new File(GIT_REPO_DIR)).pull().setCredentialsProvider(new UsernamePasswordCredentialsProvider(USERNAME, PASSWORD)).call();
        } catch (Exception e) {
            log.error("拉取Git仓库时发生异常", e);
        }
    }


    /*
     * 检查 Git仓库是否有更新
     */
    public boolean checkForUpdates(String USERNAME, String PASSWORD, String GIT_REPO_DIR) throws Exception {
        File repoDir = new File(GIT_REPO_DIR);
        try (Git git = Git.open(repoDir)) {
            // 1. 拉取远程最新信息
            FetchResult fetchResult = git.fetch().setCredentialsProvider(new UsernamePasswordCredentialsProvider(USERNAME, PASSWORD)).call();
            if (fetchResult.getTrackingRefUpdates().isEmpty()) {
                // 没有更新
                return false;
            }

            // 2. 获取本地分支和远程分支的 Commit ID
            Repository repository = git.getRepository();
            String localBranch = "refs/heads/master"; // 本地分支
            String remoteBranch = "refs/remotes/origin/master"; // 远程跟踪分支

            ObjectId localId = repository.resolve(localBranch);
            ObjectId remoteId = repository.resolve(remoteBranch);

            if (localId == null || remoteId == null) {
                throw new IOException("无法解析分支 Commit ID");
            }

            // 3. 比较 Commit ID 是否一致
            return !localId.equals(remoteId);
        }
    }

    /*
     * 构建并部署
     */
    private void buildAndDeploy(String GIT_REPO_DIR, String TARGET_DIR, String WAR_FILE_NAME, String DEST_DIR) throws Exception {

        executeCommand("mvn clean".split(" "), GIT_REPO_DIR);
        StringBuffer b = executeCommand("mvn package -Dmaven.test.skip=true".split(" "), GIT_REPO_DIR);

        log.info("Maven构建执行完成");

        // 检查构建是否成功
        if (StrUtil.contains(b, "BUILD SUCCESS")) {
            log.info("Maven构建成功");
            // 移动WAR文件到目标目录
            Path sourcePath = Paths.get(GIT_REPO_DIR, TARGET_DIR, WAR_FILE_NAME);
            Path destPath = Paths.get(DEST_DIR, WAR_FILE_NAME);

            if (Files.exists(sourcePath)) {
                //删除war文件
                FileUtil.del(destPath);
                //删除目录
                FileUtil.del(Paths.get(DEST_DIR + WAR_FILE_NAME.replace(".war", "")));

                // 复制文件
                FileUtil.copy(sourcePath, destPath);

                log.info("WAR文件已成功部署到: {}", DEST_DIR);
            } else {
                log.info("构建成功但未找到WAR文件");
            }
        } else {
            log.info("Maven构建失败");
        }
    }

    /**
     * 执行命令并获取输出
     *
     * @param command 要执行的命令
     * @param workDir 工作目录，可以为 null（表示使用当前目录）
     * @return 命令执行后的输出结果
     */
    public StringBuffer executeCommand(String[] command, String workDir) {
        Process process = null;
        StringBuffer output = new StringBuffer();
        try {
            log.info("开始执行命令: {}", Arrays.toString(command));
            // 执行命令
            if (workDir != null && !workDir.isEmpty()) {
                process = Runtime.getRuntime().exec(command, null, new java.io.File(workDir));
            } else {
                process = Runtime.getRuntime().exec(command);
            }

            // 获取进程的标准输出流和错误输出流
            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();

            // 启动线程读取标准输出
            Thread outputThread = new Thread(() -> {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        log.info(line);
                        output.append(line).append("\n");
                    }
                } catch (IOException e) {
                    log.info("Error reading output stream: {}", e.getMessage());
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        log.info("Error closing input stream: {}", e.getMessage());
                    }
                }
            });

            // 启动线程读取错误输出
            Thread errorThread = new Thread(() -> {
                BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        log.error(line);
                        output.append(line).append("\n");
                    }
                } catch (IOException e) {
                    log.info("Error reading error stream: {}", e.getMessage());
                } finally {
                    try {
                        errorStream.close();
                    } catch (IOException e) {
                        log.info("Error closing error stream: {}", e.getMessage());
                    }
                }
            });

            outputThread.start();
            errorThread.start();

            // 等待进程结束
            process.waitFor();


        } catch (IOException | InterruptedException e) {
            log.info("Error executing command: {}", e.getMessage());

        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return output;
    }
}
