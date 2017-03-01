package io.mycat.db.autotest.autoTestCheckPerformance.cache;

import io.mycat.db.autotest.bean.ProjectConfig;
import io.mycat.db.autotest.parsing.ParsingAnalysisMain;
import io.mycat.db.autotest.parsing.UseCaseParsing;
import io.mycat.db.autotest.server.cache.MapdbCache;
import io.mycat.db.autotest.utils.PathUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by qiank on 2017/3/1.
 */
public class AutoTestCacheUtils {

    /**
     * 检测xml文件有没有被修改过
     * @param projectConfigPath
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public static boolean isNotChange(String projectConfigPath) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        if(projectConfigPath == null){
            return false;
        }
        File fileF = new File(projectConfigPath,"config.xml");
        if(!fileF.exists()){
            return false;
        }
        String projectConfigId = getMd5(projectConfigPath);
        boolean flag = true;
        if(!MapdbCache.isCaseConfigStatusCache(projectConfigId,fileF.lastModified())){
            flag = false;
            MapdbCache.setCaseConfigStatusCache(projectConfigId,fileF.lastModified());
        }

        ProjectConfig projectConfig = MapdbCache.getUseCaseConfigCache("projectConfig");
        if(projectConfig == null){
            return false;
        }

        String path = projectConfig.getPath();
        List<File> paths = ParsingAnalysisMain.getFiles(PathUtils.getPath(projectConfigPath,path));
        for (File s : paths) {
            File file = new File(s,"initTestGroup.xml");
            if(!file.exists()){
                flag = false;
            }
            String testGroupId = getMd5(file.getPath());
            if(!MapdbCache.isCaseConfigStatusCache(testGroupId,file.lastModified())){
                flag = false;
                MapdbCache.setCaseConfigStatusCache(testGroupId,file.lastModified());
            }
            if(file.exists()){
                List<File> paths2 = ParsingAnalysisMain.getFiles(s);
                for (File s1 : paths2) {
                    File file2 = new File(s1,"useCase.xml");
                    if(!file2.exists()){
                        flag = false;
                    }
                    String useCaseId = getMd5(file2.getPath());
                    if(!MapdbCache.isCaseConfigStatusCache(useCaseId,file2.lastModified())){
                        flag = false;
                        MapdbCache.setCaseConfigStatusCache(useCaseId,file2.lastModified());
                    }
                }
            }
        }

        return flag;
    }

    /**
     * 获取MD5加密
     *
     * @param md5
     *            需要加密的字符串
     * @return String字符串 加密后的字符串
     */
    public static String getMd5(String md5) throws NoSuchAlgorithmException {
            // 创建加密对象
            MessageDigest digest = MessageDigest.getInstance("md5");

            // 调用加密对象的方法，加密的动作已经完成
            byte[] bs = digest.digest(md5.getBytes());
            // 接下来，我们要对加密后的结果，进行优化，按照mysql的优化思路走
            // mysql的优化思路：
            // 第一步，将数据全部转换成正数：
            String hexString = "";
            for (byte b : bs) {
                // 第一步，将数据全部转换成正数：
                // 解释：为什么采用b&255
                /*
                 * b:它本来是一个byte类型的数据(1个字节) 255：是一个int类型的数据(4个字节)
                 * byte类型的数据与int类型的数据进行运算，会自动类型提升为int类型 eg: b: 1001 1100(原始数据)
                 * 运算时： b: 0000 0000 0000 0000 0000 0000 1001 1100 255: 0000
                 * 0000 0000 0000 0000 0000 1111 1111 结果：0000 0000 0000 0000
                 * 0000 0000 1001 1100 此时的temp是一个int类型的整数
                 */
                int temp = b & 255;
                // 第二步，将所有的数据转换成16进制的形式
                // 注意：转换的时候注意if正数>=0&&<16，那么如果使用Integer.toHexString()，可能会造成缺少位数
                // 因此，需要对temp进行判断
                if (temp < 16 && temp >= 0) {
                    // 手动补上一个“0”
                    hexString = hexString + "0" + Integer.toHexString(temp);
                } else {
                    hexString = hexString + Integer.toHexString(temp);
                }
            }
            return hexString;

    }
}
