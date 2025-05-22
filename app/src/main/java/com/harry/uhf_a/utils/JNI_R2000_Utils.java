package com.harry.uhf_a.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class JNI_R2000_Utils {
    private static final String TAG = "JNI_R2000_Utils";


    /**
     * 打开通讯端口
     * @return boolean
     */
    public boolean openR2000() {
        int ret = -1;
        try {
            check();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        ret = open_tty_hsl1();
        if (ret == 0)
            return true;
        else
            return false;
    }

    /**
     * 读取版本号
     * @return VersionParam
     */
    public VersionParam getVersionParam() {
        int ret = -1;
        byte[] bytes = new byte[2];
        ret = cmd_get_version(bytes);
        if (ret == 0) {
            return new VersionParam(bytes[0],bytes[1]);
        }

        return null;

    }

    /**
     * 设置工作天线
     * @param antennaParam
     * @return boolean
     */
    public boolean setAntennaParam(AntennaParam antennaParam) {
        int ret = -1;
        ret = cmd_set_work_antenna((byte) (0x01 << (antennaParam.antennaNumber - 1)));
        if (ret == 0)
            return true;
        else
            return false;
    }

    /**
     * 获取工作天线
     * @return boolean
     */
    public AntennaParam getAntennaParam() {
        int ret = -1;
        byte[] bytes = new byte[1];
        ret = cmd_get_work_antenna(bytes);
        if (ret == 0)
            return new AntennaParam(bytes[0]+1);
       return null;
    }

    /**
     * 设置射频输出功率
     * @param powerParam
     * @return boolean
     */
    public boolean setPowerParam(PowerParam powerParam) {
        int ret = -1;
        if(powerParam.power>=20 && powerParam.power<=33){
            ret = cmd_set_output_power((byte) (powerParam.power&0xff));
            if (ret == 0)
                return true;
            else
                return false;
        }else
            return false;

    }

    /**
     * 获取射频输出功率
     * @return PowerParam
     */
    public PowerParam getPowerParam() {
        int ret = -1;
        byte[] bytes = new byte[1];

        ret = cmd_get_output_power(bytes);
        if (ret == 0)
            return new PowerParam(bytes[0]);

        return null;
    }


    /**
     * 获取射频频率范围
     * @return FreqParam
     */
    public FreqParam getFreqParam(){
        int ret = -1;
        byte[] bytes = new byte[6];
        byte[] bytesLen = new byte[1];
        RegionEnum regionEnum = null;
        float startFreq;
        float endFreq;
        ret = cmd_get_frequency(bytes,bytesLen);
        if(ret == 0){
            if(RegionEnum.FCC.getValue() == bytes[0]){
                regionEnum = RegionEnum.FCC;
            }
            if(RegionEnum.ETSI.getValue() == bytes[0]){
                regionEnum = RegionEnum.ETSI;
            }
            if(RegionEnum.CHN.getValue() == bytes[0]){
                regionEnum = RegionEnum.CHN;
            }
            startFreq = frequencyMap(bytes[1]);
            endFreq = frequencyMap(bytes[2]);
            return new FreqParam(regionEnum,startFreq,endFreq);
        }else{
           return null;
        }
       
    }

    /**
     * 设置射频频率范围
     * @param freqParam
     * @return
     */
    public boolean setFreqParam(FreqParam freqParam) {
        int ret = -1;
        ret = cmd_set_frequency(
                freqParam.regionEnum.getValue(),
                frequencyMap(freqParam.startFreq),
                frequencyMap(freqParam.endFreq));
        if (ret == 0)
            return true;
        else
            return false;
    }

    /**
     * EPC1G2实时盘点标签
     * @return
     */
    public ArrayList<Epc1g2InventoryParam> epc1g2RealTimeInventory() {


        ArrayList<Epc1g2InventoryParam> tagArray = new ArrayList<>();
        byte [][] _tagBuffer = new byte[500*4][80];
        int[] _tagNum = new int[2];
        int tagNum = 0;
        int _maskAddr = 0;
        int _maskBitLen = 0;
        byte[] _maskData = new byte[64];

        int ret = -1;
        ret = cmd_epc1g2_realtime_inventory(_tagBuffer, _tagNum, _maskAddr, _maskBitLen, _maskData);
        if(ret == 0) {
            tagNum = _tagNum[0];

            for (int i = 0; i < tagNum; i++) {
                byte thisTagEpcLen = (_tagBuffer[i][15]);
                if (thisTagEpcLen == 0 || thisTagEpcLen > 32) {
                    continue;
                }
                byte[] thisTagEpcByte = new byte[thisTagEpcLen];
                System.arraycopy(_tagBuffer[i], 16, thisTagEpcByte, 0, thisTagEpcLen);
                byte[] thisTagPcByte = new byte[2];
                System.arraycopy(_tagBuffer[i], 9, thisTagPcByte, 0, 2);
                tagArray.add(new Epc1g2InventoryParam(
                        frequencyMap((byte)_tagBuffer[i][8]),
                        _tagBuffer[i][0]+1,
                        bytesToHexString(thisTagPcByte),
                        bytesToHexString(thisTagEpcByte),
                        rssiMap(_tagBuffer[i][1])
                        ));
            }
            return tagArray;
        }else
            return null;
    }

    /**
     * EPC1G2写入存储区域数据块儿参数
     * @param epc1G2BlockParam
     * @return
     */
    public boolean writeEpc1g2Block(Epc1g2BlockParam epc1G2BlockParam){
        int ret = -1;
        byte[] _inBuffer = new byte[100];
        byte[] passwordBytes = hexString2Bytes(epc1G2BlockParam.passWord);
        if(passwordBytes.length != 4)
            return false;
        byte[] epcBytes = hexString2Bytes(epc1G2BlockParam.matchEpc);
        byte[] wordBytes = hexString2Bytes(epc1G2BlockParam.wordStr);
        if(wordBytes.length/2 != epc1G2BlockParam.wordCnt)
            return false;

        //EPC长度
        int _inBufferIndex = 0;
        _inBuffer[_inBufferIndex] = (byte) (epcBytes.length/2);
        _inBufferIndex ++;//1
        //EPC
        System.arraycopy(epcBytes, 0, _inBuffer, _inBufferIndex, epcBytes.length);
        _inBufferIndex = _inBufferIndex + epcBytes.length;//1+epc_len
        //MEM_BANK
        _inBuffer[_inBufferIndex] = epc1G2BlockParam.memBankEnum.getValue();
        _inBufferIndex ++;//1+epc_len+1
        //WORD_ADDR
        _inBuffer[_inBufferIndex] = (byte) epc1G2BlockParam.wordAdd;
        _inBufferIndex ++;//1+epc_len+1+1
        //WORD_CNT
        _inBuffer[_inBufferIndex] = (byte) epc1G2BlockParam.wordCnt;
        _inBufferIndex ++;//1+epc_len+1+1
        //WORD
        System.arraycopy(wordBytes, 0, _inBuffer, _inBufferIndex, epc1G2BlockParam.wordCnt*2);
        _inBufferIndex = _inBufferIndex + epc1G2BlockParam.wordCnt*2;//1+epc_len+1+1+word_len
        //PASSWORD
        System.arraycopy(passwordBytes, 0, _inBuffer, _inBufferIndex, 4);
        _inBufferIndex = _inBufferIndex + 4;//1+epc_len+1+1+word_len+pass_len = all_len

        ret = cmd_epc1g2_write_block(_inBuffer,_inBufferIndex);
        if (ret == 0)
            return true;
        else
            return false;

    }

    /**
     * EPC1G2读取存储区域数据块儿参数
     * @param epc1G2BlockParam
     * @return
     */
    public Epc1g2BlockParam readEpc1g2Block(Epc1g2BlockParam epc1G2BlockParam){

        int ret = -1;
        byte[] outBuffer = new byte[32];
        byte[] outBufferLen = new byte[2];
        byte[] _inBuffer = new byte[100];
        byte[] passwordBytes = hexString2Bytes(epc1G2BlockParam.passWord);
        if(passwordBytes.length != 4)
            return null;
        byte[] epcBytes = hexString2Bytes(epc1G2BlockParam.matchEpc);
        byte[] wordBytes = new byte[epc1G2BlockParam.wordCnt*2];


        //EPC长度
        int _inBufferIndex = 0;
        _inBuffer[_inBufferIndex] = (byte) (epcBytes.length/2);
        _inBufferIndex ++;//1
        //EPC内容
        System.arraycopy(epcBytes, 0, _inBuffer, _inBufferIndex, epcBytes.length);
        _inBufferIndex = _inBufferIndex + epcBytes.length;//1+epc_len
        //MEM_BANK
        _inBuffer[_inBufferIndex] = epc1G2BlockParam.memBankEnum.getValue();
        _inBufferIndex ++;//1+epc_len+1
        //WORD_ADDR
        _inBuffer[_inBufferIndex] = (byte) epc1G2BlockParam.wordAdd;
        _inBufferIndex ++;//1+epc_len+1+1
        //WORD_CNT
        _inBuffer[_inBufferIndex] = (byte) epc1G2BlockParam.wordCnt;
        _inBufferIndex ++;//1+epc_len+1+1
        //PASSWORD
        System.arraycopy(passwordBytes, 0, _inBuffer, _inBufferIndex, 4);
        _inBufferIndex = _inBufferIndex + 4;//1+epc_len+1+1+pass_len = all_len

        ret = cmd_epc1g2_read_block(_inBuffer, _inBufferIndex, outBuffer, outBufferLen);
        if (ret == 0){
            if(wordBytes.length != outBufferLen[0])
                return null;
            System.arraycopy(outBuffer, 0, wordBytes, 0, outBufferLen[0]);
            return new Epc1g2BlockParam(epc1G2BlockParam.memBankEnum,
                    epc1G2BlockParam.wordAdd,
                    epc1G2BlockParam.wordCnt,
                    epc1G2BlockParam.passWord,
                    epc1G2BlockParam.matchEpc,
                    bytesToHexString(wordBytes)
                    );
        } else
            return null;

    }

    /**
     * EPC1G2存储区域数据块儿参数
     */
    public static class Epc1g2BlockParam {

        private MemBankEnum memBankEnum;        //标签存储区域
        private int wordAdd;                    //读写数据的首地址（取值范围参考标签规格）
        private int wordCnt;                    //读写数据的字长（取值范围参考标签规格）
        private String passWord;                //标签访问密码（4字节）
        private String matchEpc;                //标签匹配EPC
        private String wordStr;                 //读写的数据



        public Epc1g2BlockParam(MemBankEnum memBankEnum,
                                int wordAdd,
                                int wordCnt,
                                String passWord,
                                String matchEpc,
                                String wordStr){
            this.memBankEnum = memBankEnum;
            this.wordAdd = wordAdd;
            this.wordCnt = wordCnt;
            this.passWord = passWord;
            this.matchEpc = matchEpc;
            this.wordStr = wordStr;
        }

        @Override
        public String toString() {
            return "Epc1g2BlockParam{" +
                    "memBankEnum=" + memBankEnum +
                    ", wordAdd=" + wordAdd +
                    ", wordCnt=" + wordCnt +
                    ", passWord='" + passWord + '\'' +
                    ", matchEpc='" + matchEpc + '\'' +
                    ", wordStr='" + wordStr + '\'' +
                    '}';
        }

        public MemBankEnum getMemBankEnum() {
            return memBankEnum;
        }

        public int getWordAdd() {
            return wordAdd;
        }

        public int getWordCnt() {
            return wordCnt;
        }

        public String getPassWord() {
            return passWord;
        }

        public String getMatchEpc() {
            return matchEpc;
        }

        public String getWordStr() {
            return wordStr;
        }
    }

    public enum MemBankEnum {

        RESERVED((byte)0x00),   //预留区
        EPC((byte)0x01),        //EPC区
        TID ((byte)0x02),       //TID区
        USER((byte)0x03);       //USER区

        private final byte tagMemBank;

        MemBankEnum(byte b) {
            this.tagMemBank = b;
        }

        public byte getValue() {
            return tagMemBank;
        }
    }

    /**
     * EPC1G2实时盘点标签参数
     */
    public static class Epc1g2InventoryParam {

        private float freq;         //频点参数
        private int ant;            //天线号
        private String pc;          //标签的PC，固定2个字节
        private String epc;         //标签的EPC，长度不固定
        private int rssi;           //标签的实时RSSI

        public Epc1g2InventoryParam(float freq, int ant, String pc, String epc, int rssi) {
            this.freq = freq;
            this.ant = ant;
            this.pc = pc;
            this.epc = epc;
            this.rssi = rssi;
        }

        @Override
        public String toString() {
            return "Epc1g2InventoryParam{" +
                    "freq=" + freq +
                    ", ant=" + ant +
                    ", pc='" + pc + '\'' +
                    ", epc='" + epc + '\'' +
                    ", rssi='" + rssi + '\'' +
                    '}';
        }

        public float getFreq() {
            return freq;
        }

        public int getAnt() {
            return ant;
        }

        public String getPc() {
            return pc;
        }

        public String getEpc() {
            return epc;
        }

        public int getRssi() {
            return rssi;
        }
    }

    /**
     * 射频频率范围
     */
    public static class FreqParam {
        private RegionEnum regionEnum;
        private float startFreq;
        private float endFreq;
        
        public FreqParam(RegionEnum regionEnum, float startFreq, float endFreq){
            this.regionEnum = regionEnum;
            this.startFreq = startFreq;
            this.endFreq = endFreq;
        }
        @Override
        public String toString() {
            return "FreqParam{" +
                    "regionEnum=" + regionEnum +
                    ", startFreq=" + startFreq +
                    ", endFreq=" + endFreq +
                    '}';
        }

        public RegionEnum getRegionEnum() {
            return regionEnum;
        }

        public float getStartFreq() {
            return startFreq;
        }

        public float getEndFreq() {
            return endFreq;
        }
    }

    public enum RegionEnum{

        FCC((byte)0x01),
        ETSI ((byte)0x02),
        CHN((byte)0x03);

        private final byte region;

        RegionEnum(byte b) {
            this.region = b;
        }

        public byte getValue() {
            return region;
        }
    }

    /**
     * 射频输出功率
     */
    public static class PowerParam {
        private int power;

        public PowerParam(int power){
            this.power = power;

        }

        @Override
        public String toString() {
            return "PowerParam{" +
                    "power=" + power +
                    '}';
        }

        public int getPower() {
            return power;
        }
    }

    /**
     * 版本号
     */
    public static class VersionParam {
        private int major;
        private int minor;
        public VersionParam(int major, int minor){
            this.major = major;
            this.minor = minor;
        }

        @Override
        public String toString() {
            return "VersionParam{" +
                    "major=" + major +
                    ", minor=" + minor +
                    '}';
        }

        public int getMajor() {
            return major;
        }

        public int getMinor() {
            return minor;
        }
    }

    /**
     * 工作天线号
     */
    public static class AntennaParam {
        private int antennaNumber ;
        public AntennaParam(int antennaNumber){
            this.antennaNumber = antennaNumber;

        }

        @Override
        public String toString() {
            return "AntennaParam{" +
                    "antennaNumber=" + antennaNumber +
                    '}';
        }

        public int getAntennaNumber() {
            return antennaNumber;
        }
    }

    /**
     * 一些辅助算法
     */
    private int rssiMap(byte rssi) {
        int index = 0;
        int[] rssiTab = {
                99, 98, 97, 96, 95, 94, 93, 92, 91, 90,
                89, 88, 87, 86, 85, 84, 83, 82, 81, 80,
                79, 78, 77, 76, 75, 74, 73, 72, 71, 70,
                69, 68, 67, 66, 65, 64, 63, 62, 61, 60,
                59, 58, 57, 56, 55, 54, 53, 52, 51, 50,
                49, 48, 47, 46, 45, 44, 43, 42, 41, 39,
                38, 37, 36, 35, 34, 33, 32, 31, 31, 31
        };

        if (rssi > 98)
            index = 98;
        else if (rssi < 31)
            index = 31;
        else
            index = rssi;

        index = index - 31;
        return rssiTab[index];

    }

    private byte frequencyMap(float freq){
        byte b = 0x00;
        float freqMin = 865.0f;
        float freqCut1Min = 868.0f;
        float freqCut1Max = 902.0f;
        float freqMax = 928.0f;
        int freqInt = (int) freq;
        float freqFloat = freq-freqInt;
        //865.00~868.00 0x00~0x06
        if(freq<=freqCut1Min && freq>=freqMin) {
            int intD = (int) (freqInt - freqMin) * 2;
            b = (byte) (b + intD & 0xff);
            float floatD = freqFloat - 0.0f;
            if (floatD >= 0.5f) {
                b = (byte) (b + 0x01);
            }
        }else if(freq<freqCut1Max && freq>freqCut1Min){
            b= 0x06;
        }//902.00~928.00 0x07~0x3b
        if(freq<=freqMax && freq>=freqCut1Max){
            b= 0x07;
            int intD = (int) (freqInt-freqCut1Max)*2;
            b = (byte) (b + intD&0xff);
            float floatD = freqFloat - 0.0f;
            if(floatD >=0.5f){
                b = (byte) (b+0x01);
            }
        }else if(freq>freqMax)
            b = 0x3b;

        return b;
    }

    private float frequencyMap(byte freq){
        float f = 865.0f;
        byte freqMin = 0x00;
        byte freqCut1Min = 0x06;
        byte freqCut1Max = 0x07;
        byte freqMax = 0x3b;

        if(freq<freqMin){
            f = 865.0f;
        }//865.00~868.00 0x00~0x06
        else if(freq<=freqCut1Min && freq>=freqMin) {
            f = 865.0f + (int)freq*0.5f;
        }else if(freq<freqCut1Max && freq>freqCut1Min){
            f= 868.0f;
        }//902.00~928.00 0x07~0x3b
        else if(freq<=freqMax && freq>=freqCut1Max){
            f= 902.0f + (int)(freq-freqCut1Max)*0.5f;;

        }else if(freq>freqMax)
            f = 928.0f;

        return f;
    }

    private byte[] hexString2Bytes(String s) {
        byte[] bytes;
        bytes = new byte[s.length() / 2];
        for (int i = 0; i < bytes.length; i++) {

            //十六进制转为十进制
            bytes[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2),
                    16);
        }
        return bytes;
    }

    public static String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    public static String genFunctionLogString(String funcStr,String ParamStr, String retStr){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\nCALL:\n");
        stringBuilder.append(funcStr+"\n");
        stringBuilder.append("\nPARAM:\n");
        stringBuilder.append(ParamStr+"\n");
        stringBuilder.append("\nRETURN:\n");
        stringBuilder.append(retStr+"\n");
        return  stringBuilder.toString();
    }

    private void check()throws SecurityException,
            IOException {{
        File device = new File("/dev/ttyHSL1");
//        if (!device.canRead() || !device.canWrite()) {
//            try {
//                Process process = Runtime.getRuntime().exec("/system/xbin/su");
//                DataOutputStream os = new DataOutputStream(process.getOutputStream());
//                os.writeBytes("chmod 0777 " + device.getAbsolutePath() + "\n");
//                os.writeBytes("exit" + "\n");
//                os.close();
//
//                if ((process.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
//                    throw new SecurityException();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                throw new SecurityException();
//            }
//        }
    }}


    public native int hello();
    public native int open_tty_hsl1();
    public native int open_tty_s3();

    //General Cmd
    public native byte cmd_get_version(byte[] ver);

    public native byte cmd_set_work_antenna(byte ant_param);

    public native byte cmd_get_work_antenna(byte[] ant_param);

    public native byte cmd_set_output_power(byte rf_power);

    public native byte cmd_get_output_power(byte[] power);

    public native byte cmd_get_frequency(byte[] out_buf,
                                         byte[] out_len);

    public native byte cmd_set_frequency(byte region,
                                         byte start_freq,
                                         byte end_freq);

    //EPC1G2 Card
    public native byte cmd_epc1g2_realtime_inventory(byte[][] tag_out_buff,
                                                     int[] tag_out_num,
                                                     int mask_addr,
                                                     int mask_len,
                                                     byte[] mask_data);

    public native byte cmd_epc1g2_inventory(byte[][] tag_out_buf,
                                            int[] tag_out_num,
                                            int mask_addr,
                                            int mask_len,
                                            byte[] mask_data);

    public native byte cmd_epc1g2_read_block(byte[] in_buf,
                                             int in_len,
                                             byte[] out_buf,
                                             byte[] out_len);

    public native byte cmd_epc1g2_write_block(byte[] in_buf,
                                              int in_len);

    public native byte cmd_epc1g2_set_lock(byte[] in_buf,
                                           int in_len);

    public native byte cmd_epc1g2_write_epc(byte[] in_buf,
                                            int in_len);

    public native byte cmd_epc1g2_destroy_tag(byte[] in_buf,
                                              int in_len);

    public native byte cmd_epc1g2_set_access_epc_match(byte mode,
                                                       byte[] in_buf,
                                                       int in_len);

    //ISO6B Card
    public native byte cmd_iso6b_inventory(byte[][] tag_buf,
                                           int[] tag_num);

    public native byte cmd_iso6b_read_block(byte[] in_buf,
                                            int in_len,
                                            byte[] out_buf,
                                            byte[] out_len);

    public native byte cmd_iso6b_write_block(byte[] in_buf, int in_len);

    public native byte cmd_iso6b_get_lock(byte[] in_buf,
                                          int in_len,
                                          byte[] out_lock_sta);

    public native byte cmd_iso6b_set_lock(byte[] in_buf,
                                          int in_len);

    static {
        System.loadLibrary("r2000_jni");
    }
}