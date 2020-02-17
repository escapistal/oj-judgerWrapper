package com.xc.oj;

import com.xc.oj.entity.JudgeResult;
import com.xc.oj.entity.JudgeTask;
import com.xc.oj.util.FTPUtil;
import com.xc.oj.util.ZipUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipFile;

@Component
public class JudgerWrapper extends Thread implements CommandLineRunner {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    public void test(){
        System.out.println(redisTemplate);
    }
    @Override
    public void run() {
        while (true) {
            try {
                JudgeTask judgeTask = (JudgeTask) redisTemplate.opsForList().rightPop("JudgeTask", 0, TimeUnit.SECONDS);
                if (judgeTask == null)
                    continue;
                System.out.println(judgeTask.getSubmissionId() + " " + judgeTask.getLanguage()+" "+judgeTask.getCode()+" "+judgeTask.getTestcaseMd5());
//
                File testcase=new File("testcase"+File.separator+judgeTask.getTestcaseMd5());
                if(!testcase.exists()){
                    File zip=new File("testcase"+File.separator+judgeTask.getTestcaseMd5()+".zip");
                    if(zip.exists())
                        zip.delete();
                    zip.createNewFile();
                    System.out.println(FTPUtil.download(zip));
                    testcase.mkdir();
                    ZipUtil.unzip(new ZipFile(zip),testcase);
                    System.out.println(zip.delete());
                }
                // TODO 下述评测结果为演示，需改成正经的judge过程(大坑)
                JudgeResult judgeResult=new JudgeResult();
                judgeResult.setSubmissionId(judgeTask.getSubmissionId());
                judgeResult.setDetail(new ArrayList<>());
                Random random=new Random();
                int len=random.nextInt(9)+1;
                for(int i=1;i<=len;i++){
                    HashMap<String,String> res=new HashMap<>();
                    res.put("id", String.valueOf(i));
                    res.put("execTime",String.valueOf(random.nextInt(1000)));
                    res.put("execMemory",String.valueOf(random.nextInt(1000)));
                    if(Integer.valueOf(res.get("execTime"))<=500&&Integer.valueOf(res.get("execMemory"))<=500)
                        res.put("result","AC");
                    else
                        res.put("result","WA");
                    judgeResult.getDetail().add(res);
                    Thread.sleep(500);
//                    Process ps=Runtime.getRuntime().exec(new String[]{"cmd.exe","dir"});
//                    BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
//                    String str;
//                    while((str=br.readLine())!=null)
//                        System.out.println(str);
                }
                redisTemplate.opsForList().leftPush("JudgeResult",judgeResult);
            }catch(Exception e){
//                System.out.println("tick-tok");
            }
        }
    }

    @Override
    public void run(String... args) throws Exception {
        start();
    }
}
