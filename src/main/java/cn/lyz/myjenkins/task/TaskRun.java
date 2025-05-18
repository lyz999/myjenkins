package cn.lyz.myjenkins.task;

import cn.lyz.myjenkins.entity.TaskEntity;
import cn.lyz.myjenkins.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TaskRun {
    @Autowired
    private TaskService taskService;

    String Username = "wood-9140";
    String Password = "9fd6f8b1f";

    @Scheduled(cron = "0 0/1 * * * ?")
    public void myjenkinsTask() {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setType("java");
        taskEntity.setProject("myjenkins");
        taskEntity.setGitRepoUrl("https://gitee.com/wood-9140/myjenkins.git");
        taskEntity.setGitRepoDir("/usr/local/projectRepo/myjenkins");
        taskEntity.setTargetDir("target");
        taskEntity.setWarFileName("myjenkins.war");
        taskEntity.setDestDir("/usr/local/tomcat9/webapps");
        taskEntity.setUsername(Username);
        taskEntity.setPassword(Password);
        taskService.task(taskEntity);
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    public void htmlTask() {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setType("html");
        taskEntity.setProject("lyz");
        taskEntity.setGitRepoUrl("https://gitee.com/wood-9140/lyz.git");
        taskEntity.setGitRepoDir("/usr/local/projectRepo/lyz");
        taskEntity.setDestDir("/lyz/www/LYZ");
        taskEntity.setUsername(Username);
        taskEntity.setPassword(Password);
        taskService.task(taskEntity);
    }
}
