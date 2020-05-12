package com.xc.oj;

import com.alibaba.fastjson.JSON;
import com.xc.oj.entity.*;
import com.xc.oj.util.FTPUtil;
import com.xc.oj.util.ZipUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static com.xc.oj.entity.JudgeResultEnum.*;

@Component
public class JudgerWrapper extends Thread implements CommandLineRunner {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    private static Map<String,String> ruleMap=new HashMap<String, String>() {{
        put("C++","c_cpp");
        put("C","c_cpp");
        put("Python 2","general");
        put("Python 3","general");
    }};

    private void saveToFile(String code,File codeFile) throws IOException {
        if(codeFile.exists())
            codeFile.delete();
        codeFile.createNewFile();
        FileWriter codeFileWriter=new FileWriter(codeFile);
        codeFileWriter.write(code);
        codeFileWriter.close();
    }

    private int executeCompiler(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        processBuilder.redirectErrorStream(true);
        Process process=processBuilder.start();
        BufferedReader out=new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while((line=out.readLine())!=null) {
            System.out.println(line);
        }
        out.close();
        int ret=process.waitFor();
        System.out.println("compile res:"+ret);
        return ret;
    }

    private JudgerLibResult executeRunner(ProcessBuilder processBuilder) throws InterruptedException, IOException {
        processBuilder.redirectErrorStream(true);
        Process process=processBuilder.start();
        BufferedReader out=new BufferedReader(new InputStreamReader(process.getInputStream()));
        String lines = new String(),line;
        while((line=out.readLine())!=null)
            lines+=line;
        out.close();
        process.waitFor();
        return JSON.parseObject(lines, JudgerLibResult.class);
    }

    private File compile(File targetDir, String lang, String code) throws IOException, InterruptedException {
        if ("C".equalsIgnoreCase(lang)) {
            File codeFile = new File(targetDir.getAbsolutePath() + File.separator + "main.c");
            saveToFile(code, codeFile);
            File exe = new File(targetDir.getAbsolutePath() + File.separator + "main");
            ProcessBuilder processBuilder = new ProcessBuilder(
                    new String[]{"gcc", "-O2", "-std=c11", codeFile.getAbsolutePath(), "-o", exe.getAbsolutePath()});
            if (executeCompiler(processBuilder) != 0)
                return null;
            return exe;
        } else if ("C++".equalsIgnoreCase(lang)) {
            File codeFile = new File(targetDir.getAbsolutePath() + File.separator + "main.cpp");
            saveToFile(code, codeFile);
            File exe = new File(targetDir.getAbsolutePath() + File.separator + "main");
            ProcessBuilder processBuilder = new ProcessBuilder(
                    new String[]{"g++", "-O2", "-std=c++17", codeFile.getAbsolutePath(), "-o", exe.getAbsolutePath()});
            if (executeCompiler(processBuilder) != 0)
                return null;
            return exe;
        } else if ("Java".equalsIgnoreCase(lang)) {
            File codeFile = new File(targetDir.getAbsolutePath() + File.separator + "Main.java");
            saveToFile(code, codeFile);
            File exe = new File(targetDir.getAbsolutePath() + File.separator + "Main.class");
            ProcessBuilder processBuilder = new ProcessBuilder(
                    new String[]{"javac", codeFile.getAbsolutePath(), "-d", targetDir.getAbsolutePath(), "-encoding", "UTF8"});
            System.out.println(Arrays.toString(
                    new String[]{"javac", codeFile.getAbsolutePath(), "-d", targetDir.getAbsolutePath(), "-encoding", "UTF8"}));
            if (executeCompiler(processBuilder) != 0)
                return null;
            return exe;
        } else if ("Python 3".equalsIgnoreCase(lang)) {
            File codeFile = new File(targetDir.getAbsolutePath() + File.separator + "main.py");
            saveToFile(code, codeFile);
            File exe = new File(targetDir.getAbsolutePath() + File.separator +
                    "__pycache__"+File.separator+"main.cpython-38.pyc");
            ProcessBuilder processBuilder = new ProcessBuilder(
                    new String[]{"python3", "-m", "py_compile", codeFile.getAbsolutePath()});
            if (executeCompiler(processBuilder) != 0)
                return null;
            return exe;
        } else if ("Python 2".equalsIgnoreCase(lang)) {
            File codeFile = new File(targetDir.getAbsolutePath() + File.separator + "main.py");
            saveToFile(code, codeFile);
            File exe = new File(targetDir.getAbsolutePath() + File.separator + "main.pyc");
            ProcessBuilder processBuilder = new ProcessBuilder(
                    new String[]{"python2", "-m", "py_compile", codeFile.getAbsolutePath()});
            if (executeCompiler(processBuilder) != 0)
                return null;
            return exe;
        }
        else
            return null;
    }

    private String rtrim(String value) {
        int len = value.length();
        while (len>=1&&value.charAt(len-1)<=' ')
            len--;
        return len>=1?value.substring(0, len):"";
    }
    private boolean cmp(File std,File usr) throws IOException {
        boolean res=true;
        BufferedReader stdReader=new BufferedReader(new FileReader(std));
        BufferedReader usrReader=new BufferedReader(new FileReader(usr));
        String a,b;
        while(true){
            a=stdReader.readLine();
            b=usrReader.readLine();
            if(a==null||b==null) {
                if(b!=null){
                    do {
                        if(!"".equals(rtrim(b)))
                            res=false;
                        b=usrReader.readLine();
                    }while(res&&b!=null);
                }
                if(a!=null){
                    do {
                        if(!"".equals(rtrim(a)))
                            res=false;
                        a=stdReader.readLine();
                    }while(res&&a!=null);
                }
                break;
            }
            if(!rtrim(a).equals(rtrim(b))){
                res=false;
                break;
            }
        }
        stdReader.close();
        usrReader.close();
        return res;
    }
    /*
    ./libjudger.so --max_cpu_time=1000 --max_real_time=1000 --max_memory=134217728 --max_stack=134217728 --max_process_number=200
    --max_output_size=134217728 --exe_path="test" --input_path="input" --output_path="echo.out" --error_path="echo.out"
    --args=[] --env=[] --log_path="judger.log" --seccomp_rule_name="c_cpp" --uid=0 --gid=0
    */
    private SingleJudgeResult judge(File exe,File in,File out,
                                    int timeLimit,int memoryLimit,String lang) throws IOException, InterruptedException {
        List<String> rules=new ArrayList<>();
        String output_path=null;
        if("C".equalsIgnoreCase(lang)||"C++".equalsIgnoreCase(lang)) {
            rules.add("./libjudger.so");
            rules.add("--max_cpu_time=" + (timeLimit + 0));
            rules.add("--max_real_time=" + (timeLimit + timeLimit));
            rules.add("--max_memory=" + (memoryLimit * 1024L * 1024L));
            rules.add("--max_stack=" + (memoryLimit * 1024L * 1024L));
            rules.add("--max_process_number=" + 200);
            rules.add("--max_output_size=" + 134217728);
            rules.add("--exe_path=" + exe.getAbsolutePath());
            rules.add("--input_path=" + in.getAbsolutePath());
            rules.add("--output_path=" + exe.getParent() + File.separator + "user.out");
            rules.add("--error_path=" + exe.getParent() + File.separator + "user.err");
            rules.add("--log_path=" + exe.getParent() + File.separator + "judger.log");
            rules.add("--uid=" + 0);
            rules.add("--gid=" + 0);
            if (ruleMap.get(lang) != null)
                rules.add("--seccomp_rule_name=" + ruleMap.get(lang));
            output_path=exe.getParent() + File.separator + "user.out";
        }
        else if("Java".equalsIgnoreCase(lang)){
            rules.add("./libjudger.so");
            rules.add("--max_cpu_time=" + (timeLimit + 0));
            rules.add("--max_real_time=" + (timeLimit + timeLimit));
            rules.add("--max_memory=" + "-1");
            rules.add("--max_stack=" + 1342177280);
            rules.add("--max_process_number=" + 200);
            rules.add("--max_output_size=" + 1342177280);
            rules.add("--exe_path=" + "/usr/bin/jdk-14.0.1/bin/java");
            rules.add("--input_path=" + in.getAbsolutePath());
            rules.add("--output_path=" + exe.getParent() + File.separator + "user.out");
            rules.add("--error_path=" + exe.getParent() + File.separator + "user.err");
            rules.add("--log_path=" + exe.getParent() + File.separator + "judger.log");
            rules.add("--args="+"-cp "+exe.getParent()+" Main"+"");
            rules.add("--uid=" + 0);
            rules.add("--gid=" + 0);
            rules.add("--memory_limit_check_only="+0);
            output_path=exe.getParent() + File.separator + "user.out";
        }
        else if("Python 3".equalsIgnoreCase(lang)){
            rules.add("./libjudger.so");
            rules.add("--max_cpu_time=" + (timeLimit + 0));
            rules.add("--max_real_time=" + (timeLimit + timeLimit));
            rules.add("--max_memory=" + (memoryLimit * 1024L * 1024L));
            rules.add("--max_stack=" + (memoryLimit * 1024L * 1024L));
            rules.add("--max_process_number=" + 200);
            rules.add("--max_output_size=" + 134217728);
            rules.add("--exe_path="+"/usr/bin/python3");
            rules.add("--input_path=" + in.getAbsolutePath());
            rules.add("--output_path=" + exe.getParentFile().getParent() + File.separator + "user.out");
            rules.add("--error_path=" + exe.getParentFile().getParent() + File.separator + "user.err");
            rules.add("--log_path=" + exe.getParentFile().getParent() + File.separator + "judger.log");
            rules.add("--args="+exe.getAbsolutePath());
            rules.add("--uid=" + 0);
            rules.add("--gid=" + 0);
            if (ruleMap.get(lang) != null)
                rules.add("--seccomp_rule_name=" + ruleMap.get(lang));
            output_path=exe.getParentFile().getParent() + File.separator + "user.out";
        } else if("Python 2".equalsIgnoreCase(lang)){
            rules.add("./libjudger.so");
            rules.add("--max_cpu_time=" + (timeLimit + 0));
            rules.add("--max_real_time=" + (timeLimit + timeLimit));
            rules.add("--max_memory=" + (memoryLimit * 1024L * 1024L));
            rules.add("--max_stack=" + (memoryLimit * 1024L * 1024L));
            rules.add("--max_process_number=" + 200);
            rules.add("--max_output_size=" + 134217728);
            rules.add("--exe_path="+"/usr/bin/python2");
            rules.add("--input_path=" + in.getAbsolutePath());
            rules.add("--output_path=" + exe.getParent() + File.separator + "user.out");
            rules.add("--error_path=" + exe.getParent() + File.separator + "user.err");
            rules.add("--log_path=" + exe.getParent() + File.separator + "judger.log");
            rules.add("--args="+exe.getAbsolutePath());
            rules.add("--uid=" + 0);
            rules.add("--gid=" + 0);
            if (ruleMap.get(lang) != null)
                rules.add("--seccomp_rule_name=" + ruleMap.get(lang));
            output_path=exe.getParent() + File.separator + "user.out";
        }
        ProcessBuilder processBuilder=new ProcessBuilder(rules.toArray(new String[rules.size()]));
        JudgerLibResult libResult=executeRunner(processBuilder);
        SingleJudgeResult judgeResult=new SingleJudgeResult();
        judgeResult.setExecTime(libResult.getCpu_time());
        judgeResult.setExecMemory((int) (libResult.getMemory()/1024));
        switch (libResult.getResult()){
            case 0:
                judgeResult.setResult(
                        cmp(out,new File(output_path))?AC:WA);
                break;
            case 1: case 2:
                judgeResult.setResult(TLE);
                break;
            case 3:
                judgeResult.setResult(MLE);
                break;
            case 4:
                judgeResult.setResult(RE);
                break;
            case 5:
                judgeResult.setResult(SE);
        }
        System.out.println(libResult.getCpu_time());
        System.out.println(libResult.getReal_time());
        System.out.println(libResult.getMemory());
        System.out.println(libResult.getSignal());
        System.out.println(libResult.getExit_code());
        System.out.println(libResult.getResult());
        System.out.println();
        return judgeResult;
    }

    private JudgeResult judge(JudgeTask judgeTask){
        JudgeResult judgeResult = new JudgeResult();
        judgeResult.setSubmissionId(judgeTask.getSubmissionId());
        judgeResult.setDetail(new ArrayList<>());
        File zip=null;
        try {
            File testcaseDir = new File("testcase" + File.separator + judgeTask.getTestcaseMd5());
            if (!testcaseDir.exists()) {
                System.out.println("Testcase not exist! md5:" + judgeTask.getTestcaseMd5() + " Downloading ...");
                zip = new File("testcase" + File.separator + judgeTask.getTestcaseMd5() + ".zip");
                if (zip.exists())
                    zip.delete();
                zip.createNewFile();
                boolean downloadResult = FTPUtil.download(zip);
                System.out.println("Download Result:" + downloadResult);
                if (!downloadResult) {
                    System.out.println("Download Failed! Passing JudgeTask ...");
                    throw new Exception();
                }
                testcaseDir.mkdir();
                ZipUtil.unzip(new ZipFile(zip), testcaseDir);
                zip.delete();
            }
            File workDir=new File("submission"+File.separator+judgeTask.getSubmissionId());
            workDir.mkdir();
            File exe = compile(workDir,judgeTask.getLanguage(), judgeTask.getCode());
            if (exe==null||!exe.exists())
                judgeResult.setResult(CE);
            else {
                int cases=testcaseDir.listFiles().length/2;
                for(int i=1;i<=cases;i++){
                    SingleJudgeResult res=judge(exe,
                            new File(testcaseDir.getAbsolutePath()+File.separator+i+".in"),
                            new File(testcaseDir.getAbsolutePath()+File.separator+i+".out"),
                            judgeTask.getTimeLimit(),judgeTask.getMemoryLimit(),
                            judgeTask.getLanguage());
                    System.out.println(res.getResult()+" "+res.getExecTime()+" "+res.getExecMemory());
                    judgeResult.getDetail().add(res);
                }
            }
        } catch (InterruptedException e) {
            judgeResult.setResult(SE);
        } catch (ZipException e) {
            judgeResult.setResult(SE);
        } catch (IOException e) {
            judgeResult.setResult(SE);
        } catch (Exception e) {
            judgeResult.setResult(SE);
        } finally {
            if(zip!=null&&zip.exists())
                zip.delete();
        }
        return judgeResult;
    }

    @Override
    public void run() {
        File testcaseDir=new File("testcase");
        File submissionDir=new File("submission");
        if(!testcaseDir.exists())
            testcaseDir.mkdir();
        if(!submissionDir.exists())
            submissionDir.mkdir();
        while (true) {
            try {
                JudgeTask judgeTask = (JudgeTask) redisTemplate.opsForList().rightPop("JudgeTask", 0, TimeUnit.SECONDS);
                if (judgeTask == null)
                    continue;
                System.out.println(judgeTask.getSubmissionId() + " " + judgeTask.getLanguage()+" "+judgeTask.getCode()+" "+judgeTask.getTestcaseMd5());
//                System.out.println(judgeTask.getTimeLimit()+" "+judgeTask.getMemoryLimit());
                JudgeResult judgeResult=judge(judgeTask);
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
