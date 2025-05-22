package com.harry.uhf_a.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Created by Administrator on 2018/11/28 0028.
 */

public class JNI_GPIO_Utils {



    private final static int GPIO_DIRECTION_OUT = 1;
    private final static int GPIO_DIRECTION_IN = 0;

    private final static int GPIO_LED_PWR = 10+911;  //led2 gpio10
    private final static int GPIO_LED_RFID = 9+911;  //led3  gpio9
    private final static int GPIO_LED_COM = 88+911;  //led4  gpio88
    private final static int GPIO_LED_WIFI = 99+911; //led5  gpio99
    private final static int GPIO_LED_4G = 58+911;   //led6  gpio58

    private final static int GPIO_BUZZER = 96+911;

    private final static int GPIO_TRIGGER1 = 89+911;
    private final static int GPIO_TRIGGER2 = 69+911;
    private final static int GPIO_TRIGGER3 = 34+911;
    private final static int GPIO_TRIGGER4 = 35+911;

    private final static int GPIO_RELAY1 = 33+911;
    private final static int GPIO_RELAY2 = 28+911;
    private final static int GPIO_RELAY3 = 26+911;
    private final static int GPIO_RELAY4 = 27+911;

    private final static int GPIO_RFID_EN = 16+911;

    private final static int GPIO_E1 = 110+911;
    private final static int GPIO_E2 = 0+911;
    private final static int GPIO_E3 = 65+911;
    private final static int GPIO_E4__GPIO_BL = 36+911;
    private final static int GPIO_E5__GP1O_M1 = 68+911;
    private final static int GPIO_E6__GPIO_M2 = 97+911;


    private LedStatusEnum ledPwr,ledRfid,ledCom,ledWifi,led4g;
    private BuzzerStatusEnum buzzer;
    private TriggerStatusEnum triiger1,triiger2,trigger3,trigger4;
    private RelayStatusEnum relay1,relay2,relay3,relay4;
    private RfidEnStatusEnum rfidEn;
    private ExGpioStatusEnum exGpio1,exGpio2,exGpio3,exGpio4,exGpio5,exGpio6;
    private MotorStatusEnum motor;

    public LedStatusEnum getLedPwr() {
        return ledPwr;
    }

    public void setLedPwr(LedStatusEnum ledPwr) {
        this.ledPwr = ledPwr;

        if (ledPwr == LedStatusEnum.STATUS_BLINK) {
            synchronized(this){
                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        try {
                            Thread.sleep(80);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        doWriteGpioStatus(GPIO_LED_PWR, LedStatusEnum.STATUS_ON.getValue());
                        try {
                            Thread.sleep(80);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        doWriteGpioStatus(GPIO_LED_PWR, LedStatusEnum.STATUS_OFF.getValue());

                    }
                }.start();

            }
        }else {
            doWriteGpioStatus(GPIO_LED_PWR,ledPwr.getValue());
        }
    }

    public LedStatusEnum getLedRfid() {
        return ledRfid;
    }

    public void setLedRfid(LedStatusEnum ledRfid) {
        this.ledRfid = ledRfid;

        if (ledRfid == LedStatusEnum.STATUS_BLINK) {

            synchronized(this){
                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        try {
                            Thread.sleep(80);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        doWriteGpioStatus(GPIO_LED_RFID, LedStatusEnum.STATUS_ON.getValue());
                        try {
                            Thread.sleep(80);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        doWriteGpioStatus(GPIO_LED_RFID, LedStatusEnum.STATUS_OFF.getValue());

                    }
                }.start();

            }
        }else {
            doWriteGpioStatus(GPIO_LED_RFID,ledRfid.getValue());
        }
    }

    public LedStatusEnum getLedCom() {
        return ledCom;
    }

    public void setLedCom(LedStatusEnum ledCom) {
        this.ledCom = ledCom;

        if (ledCom == LedStatusEnum.STATUS_BLINK) {

            synchronized(this){
                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        try {
                            Thread.sleep(80);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        doWriteGpioStatus(GPIO_LED_COM, LedStatusEnum.STATUS_ON.getValue());
                        try {
                            Thread.sleep(80);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        doWriteGpioStatus(GPIO_LED_COM, LedStatusEnum.STATUS_OFF.getValue());

                    }
                }.start();

            }
        }else {
            doWriteGpioStatus(GPIO_LED_COM,ledCom.getValue());
        }
    }

    public LedStatusEnum getLedWifi() {
        return ledWifi;
    }

    public void setLedWifi(LedStatusEnum ledWifi) {
        this.ledWifi = ledWifi;

        if (ledWifi == LedStatusEnum.STATUS_BLINK) {

            synchronized(this){
                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        try {
                            Thread.sleep(80);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        doWriteGpioStatus(GPIO_LED_WIFI, LedStatusEnum.STATUS_ON.getValue());
                        try {
                            Thread.sleep(80);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        doWriteGpioStatus(GPIO_LED_WIFI, LedStatusEnum.STATUS_OFF.getValue());

                    }
                }.start();

            }
        }else {
            doWriteGpioStatus(GPIO_LED_WIFI,ledWifi.getValue());
        }
    }

    public LedStatusEnum getLed4g() {
        return led4g;
    }

    public void setLed4g(LedStatusEnum led4g) {
        this.led4g = led4g;
        if (led4g == LedStatusEnum.STATUS_BLINK) {

            synchronized(this){
                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        try {
                            Thread.sleep(80);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        doWriteGpioStatus(GPIO_LED_4G, LedStatusEnum.STATUS_ON.getValue());
                        try {
                            Thread.sleep(80);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        doWriteGpioStatus(GPIO_LED_4G, LedStatusEnum.STATUS_OFF.getValue());

                    }
                }.start();

            }
        }else {
            doWriteGpioStatus(GPIO_LED_4G,led4g.getValue());
        }
    }

    public BuzzerStatusEnum getBuzzer() {
        return buzzer;
    }

    public void setBuzzer(BuzzerStatusEnum buzzer) {
        this.buzzer = buzzer;
        if (buzzer == BuzzerStatusEnum.STATUS_BEEP) {

            synchronized(this){
                new Thread() {
                    @Override
                    public void run() {
                        super.run();

                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        doWriteGpioStatus(GPIO_BUZZER, LedStatusEnum.STATUS_ON.getValue());
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        doWriteGpioStatus(GPIO_BUZZER, LedStatusEnum.STATUS_OFF.getValue());

                    }
                }.start();

            }
        }else {
            doWriteGpioStatus(GPIO_BUZZER, buzzer.getValue());
        }

    }

    public TriggerStatusEnum getTrigger1() {
        if(doReadGpioStatus(GPIO_TRIGGER1) == 0)
            triiger1 = TriggerStatusEnum.STATUS_ON;
        else
            triiger1 = TriggerStatusEnum.STATUS_OFF;
        return triiger1;
    }

    public TriggerStatusEnum getTrigger2() {
        if(doReadGpioStatus(GPIO_TRIGGER2) == 0)
            triiger2 = TriggerStatusEnum.STATUS_ON;
        else
            triiger2 = TriggerStatusEnum.STATUS_OFF;
        return triiger2;
    }

    public TriggerStatusEnum getTrigger3() {
        if(doReadGpioStatus(GPIO_TRIGGER3) == 0)
            trigger3 = TriggerStatusEnum.STATUS_ON;
        else
            trigger3 = TriggerStatusEnum.STATUS_OFF;
        return trigger3;
    }

    public TriggerStatusEnum getTrigger4() {
        if(doReadGpioStatus(GPIO_TRIGGER4) == 0)
            trigger4 = TriggerStatusEnum.STATUS_ON;
        else
            trigger4 = TriggerStatusEnum.STATUS_OFF;
        return trigger4;
    }


    public RelayStatusEnum getRelay1() {
        if(doReadGpioStatus(GPIO_RELAY1) == 1)
            relay1 = RelayStatusEnum.STATUS_ON;
        else
            relay1 = RelayStatusEnum.STATUS_OFF;
        return relay1;
    }

    public void setRelay1(RelayStatusEnum relay1) {
        this.relay1 = relay1;
        doWriteGpioStatus(GPIO_RELAY1,relay1.getValue());
    }

    public RelayStatusEnum getRelay2() {
        if(doReadGpioStatus(GPIO_RELAY2) == 1)
            relay2 = RelayStatusEnum.STATUS_ON;
        else
            relay2 = RelayStatusEnum.STATUS_OFF;
        return relay2;
    }

    public void setRelay2(RelayStatusEnum relay2) {
        this.relay2 = relay2;
        doWriteGpioStatus(GPIO_RELAY2,relay2.getValue());
    }

    public RelayStatusEnum getRelay3() {
        if(doReadGpioStatus(GPIO_RELAY3) == 1)
            relay3 = RelayStatusEnum.STATUS_ON;
        else
            relay3 = RelayStatusEnum.STATUS_OFF;
        return relay3;
    }

    public void setRelay3(RelayStatusEnum relay3) {
        this.relay3 = relay3;
        doWriteGpioStatus(GPIO_RELAY3,relay3.getValue());
    }

    public RelayStatusEnum getRelay4() {
        if(doReadGpioStatus(GPIO_RELAY4) == 1)
            relay4 = RelayStatusEnum.STATUS_ON;
        else
            relay4 = RelayStatusEnum.STATUS_OFF;
        return relay4;
    }

    public void setRelay4(RelayStatusEnum relay4) {
        this.relay4 = relay4;
        doWriteGpioStatus(GPIO_RELAY4,relay4.getValue());
    }

    public enum LedStatusEnum {

        STATUS_ON(1),
        STATUS_OFF(0),
        STATUS_BLINK (-1);

        private final int ledStatus;

        LedStatusEnum(int b) {
            this.ledStatus = b;
        }

        public int getValue() {
            return ledStatus;
        }
    }

    public enum BuzzerStatusEnum {

        STATUS_ON(1),
        STATUS_OFF(0),
        STATUS_BEEP (-1);

        private final int beepStatus;

        BuzzerStatusEnum(int b) {
            this.beepStatus = b;
        }

        public int getValue() {
            return beepStatus;
        }
    }

    public enum TriggerStatusEnum {

        STATUS_ON(0),
        STATUS_OFF(1);//初始状态为高电平，定义为未触发状态

        private final int triggerStatus;

        TriggerStatusEnum(int b) {
            this.triggerStatus = b;
        }

        public int getValue() {
            return triggerStatus;
        }
    }

    public enum RelayStatusEnum {

        STATUS_ON(1),
        STATUS_OFF(0),;

        private final int relayStatus;

        RelayStatusEnum(int b) {
            this.relayStatus = b;
        }

        public int getValue() {
            return relayStatus;
        }
    }

    /**
     * RFID使能脚状态枚举
     */
    public enum RfidEnStatusEnum {

        STATUS_ON(1),
        STATUS_OFF(0),
        STATUS_BEEP (-1);

        private final int levelStatus;

        RfidEnStatusEnum(int l) {
            this.levelStatus = l;
        }

        public int getValue() {
            return levelStatus;
        }
    }

    /**
     * RFID使能脚状态获取
     */
    public RfidEnStatusEnum getRfidEnStatus() {
        if(doReadGpioStatus(GPIO_RFID_EN) == 1)
            rfidEn = RfidEnStatusEnum.STATUS_ON;
        else
            rfidEn = RfidEnStatusEnum.STATUS_OFF;
        return rfidEn;
    }
    /**
     * RFID使能脚状态设置
     */
    public void setRfidEn(RfidEnStatusEnum rfidEn) {
        this.rfidEn = rfidEn;
        doWriteGpioStatus(GPIO_RFID_EN,rfidEn.getValue());
    }


    /**
     * 扩展IO状态枚举
     */
    public enum ExGpioStatusEnum {

        STATUS_OUT_HIGH(1),
        STATUS_OUT_LOW(0),
        STATUS_BEEP (-1);

        private final int levelStatus;

        ExGpioStatusEnum(int l) {
            this.levelStatus = l;
        }

        public int getValue() {
            return levelStatus;
        }
    }

    /**
     * 扩展IO状态获取
     */
    public ExGpioStatusEnum getExGpio1() {
        if(doReadGpioStatus(GPIO_E1) == 1)
            exGpio1 = ExGpioStatusEnum.STATUS_OUT_HIGH;
        else
            exGpio1 = ExGpioStatusEnum.STATUS_OUT_LOW;
        return exGpio1;
    }
    public ExGpioStatusEnum getExGpio2() {
        if(doReadGpioStatus(GPIO_E2) == 1)
            exGpio2 = ExGpioStatusEnum.STATUS_OUT_HIGH;
        else
            exGpio2 = ExGpioStatusEnum.STATUS_OUT_LOW;
        return exGpio2;
    }
    public ExGpioStatusEnum getExGpio3() {
        if(doReadGpioStatus(GPIO_E3) == 1)
            exGpio3 = ExGpioStatusEnum.STATUS_OUT_HIGH;
        else
            exGpio3 = ExGpioStatusEnum.STATUS_OUT_LOW;
        return exGpio3;
    }
    public ExGpioStatusEnum getExGpio4() {
        if(doReadGpioStatus(GPIO_E4__GPIO_BL) == 1)
            exGpio4 = ExGpioStatusEnum.STATUS_OUT_HIGH;
        else
            exGpio4 = ExGpioStatusEnum.STATUS_OUT_LOW;
        return exGpio4;
    }
    public ExGpioStatusEnum getExGpio5() {
        if(doReadGpioStatus(GPIO_E5__GP1O_M1) == 1)
            exGpio5 = ExGpioStatusEnum.STATUS_OUT_HIGH;
        else
            exGpio5 = ExGpioStatusEnum.STATUS_OUT_LOW;
        return exGpio5;
    }
    public ExGpioStatusEnum getExGpio6() {
        if(doReadGpioStatus(GPIO_E6__GPIO_M2) == 1)
            exGpio6 = ExGpioStatusEnum.STATUS_OUT_HIGH;
        else
            exGpio6 = ExGpioStatusEnum.STATUS_OUT_LOW;
        return exGpio6;
    }
    /**
     * 扩展IO状态设置函数
     */
    public void setExGpio1(ExGpioStatusEnum exGpio) {
        this.exGpio1 = exGpio;
        doWriteGpioStatus(GPIO_E1,exGpio1.getValue());
    }
    public void setExGpio2(ExGpioStatusEnum exGpio) {
        this.exGpio2 = exGpio;
        doWriteGpioStatus(GPIO_E2,exGpio2.getValue());
    }
    public void setExGpio3(ExGpioStatusEnum exGpio) {
        this.exGpio3 = exGpio;
        doWriteGpioStatus(GPIO_E3,exGpio3.getValue());
    }
    public void setExGpio4(ExGpioStatusEnum exGpio) {
        this.exGpio4 = exGpio;
        doWriteGpioStatus(GPIO_E4__GPIO_BL,exGpio4.getValue());
    }
    public void setExGpio5(ExGpioStatusEnum exGpio) {
        this.exGpio5 = exGpio;
        doWriteGpioStatus(GPIO_E5__GP1O_M1,exGpio5.getValue());
    }
    public void setExGpio6(ExGpioStatusEnum exGpio) {
        this.exGpio6 = exGpio;
        doWriteGpioStatus(GPIO_E6__GPIO_M2,exGpio6.getValue());
    }

    /**
     * 电机状态枚举
     */
    public enum MotorStatusEnum {

        STATUS_FORWARD(1),//正转
        STATUS_REVERSE(0),//反转
        STATUS_GLIDING(-1),//滑行
        STATUS_BRAKE(-2);//刹车


        private final int levelStatus;

        MotorStatusEnum(int l) {
            this.levelStatus = l;
        }

        public int getValue() {
            return levelStatus;
        }
    }

    /**
     *  电机状态获取
     */
    public MotorStatusEnum getMotor() {
        if(doReadGpioStatus(GPIO_E5__GP1O_M1) == 1 &&
                doReadGpioStatus(GPIO_E6__GPIO_M2)==0)
            motor = MotorStatusEnum.STATUS_FORWARD;
        else  if(doReadGpioStatus(GPIO_E5__GP1O_M1) == 0 &&
                doReadGpioStatus(GPIO_E6__GPIO_M2)==1)
            motor = MotorStatusEnum.STATUS_REVERSE;
        else  if(doReadGpioStatus(GPIO_E5__GP1O_M1) == 0 &&
                doReadGpioStatus(GPIO_E6__GPIO_M2)==0)
            motor = MotorStatusEnum.STATUS_GLIDING;
        else  if(doReadGpioStatus(GPIO_E5__GP1O_M1) == 1 &&
                doReadGpioStatus(GPIO_E6__GPIO_M2)==1)
            motor = MotorStatusEnum.STATUS_BRAKE;
        return motor;
    }
    /**
     *  电机状态设置
     */
    public void setMotor(MotorStatusEnum motor) {
        this.motor = motor;
        switch (motor.getValue()){
          case 0:
              doWriteGpioStatus(GPIO_E5__GP1O_M1,1);
              doWriteGpioStatus(GPIO_E6__GPIO_M2,0);
              break;
          case 1:
              doWriteGpioStatus(GPIO_E5__GP1O_M1,0);
              doWriteGpioStatus(GPIO_E6__GPIO_M2,1);
              break;
          case -1:
              doWriteGpioStatus(GPIO_E5__GP1O_M1,0);
              doWriteGpioStatus(GPIO_E6__GPIO_M2,0);
              break;
          case -2:
              doWriteGpioStatus(GPIO_E5__GP1O_M1,1);
              doWriteGpioStatus(GPIO_E6__GPIO_M2,1);
              break;
        }

    }

    public void gpioInit(){
        ledInit();
        beepInit();
        relayInit();
        triggerInit();
        exGpioInit();
        rfidEnInit();
    }

    public void ledInit(){
        doExportGpio(GPIO_LED_PWR);
        doSetGpioDirection(GPIO_LED_PWR,GPIO_DIRECTION_OUT);
        doExportGpio(GPIO_LED_RFID);
        doSetGpioDirection(GPIO_LED_RFID,GPIO_DIRECTION_OUT);
        doExportGpio(GPIO_LED_COM);
        doSetGpioDirection(GPIO_LED_COM,GPIO_DIRECTION_OUT);
        doExportGpio(GPIO_LED_WIFI);
        doSetGpioDirection(GPIO_LED_WIFI,GPIO_DIRECTION_OUT);
        doExportGpio(GPIO_LED_4G);
        doSetGpioDirection(GPIO_LED_4G,GPIO_DIRECTION_OUT);

    }

    public void beepInit(){
        doExportGpio(GPIO_BUZZER);
        doSetGpioDirection(GPIO_BUZZER,GPIO_DIRECTION_OUT);

    }

    public void triggerInit(){
        doExportGpio(GPIO_TRIGGER1);
        doSetGpioDirection(GPIO_TRIGGER1,GPIO_DIRECTION_IN);
        doExportGpio(GPIO_TRIGGER2);
        doSetGpioDirection(GPIO_TRIGGER2,GPIO_DIRECTION_IN);
        doExportGpio(GPIO_TRIGGER3);
        doSetGpioDirection(GPIO_TRIGGER3,GPIO_DIRECTION_IN);
        doExportGpio(GPIO_TRIGGER4);
        doSetGpioDirection(GPIO_TRIGGER4,GPIO_DIRECTION_IN);

    }

    public void relayInit(){
        doExportGpio(GPIO_RELAY1);
        doSetGpioDirection(GPIO_RELAY1,GPIO_DIRECTION_OUT);
        doExportGpio(GPIO_RELAY2);
        doSetGpioDirection(GPIO_RELAY2,GPIO_DIRECTION_OUT);
        doExportGpio(GPIO_RELAY3);
        doSetGpioDirection(GPIO_RELAY3,GPIO_DIRECTION_OUT);
        doExportGpio(GPIO_RELAY4);
        doSetGpioDirection(GPIO_RELAY4,GPIO_DIRECTION_OUT);

    }

    public void exGpioInit(){
        doExportGpio(GPIO_E1);
        doSetGpioDirection(GPIO_E1,GPIO_DIRECTION_OUT);
        doExportGpio(GPIO_E2);
        doSetGpioDirection(GPIO_E2,GPIO_DIRECTION_OUT);
        doExportGpio(GPIO_E3);
        doSetGpioDirection(GPIO_E3,GPIO_DIRECTION_OUT);
        doExportGpio(GPIO_E4__GPIO_BL);
        doSetGpioDirection(GPIO_E4__GPIO_BL,GPIO_DIRECTION_OUT);
        doExportGpio(GPIO_E5__GP1O_M1);
        doSetGpioDirection(GPIO_E5__GP1O_M1,GPIO_DIRECTION_OUT);
        doExportGpio(GPIO_E6__GPIO_M2);
        doSetGpioDirection(GPIO_E6__GPIO_M2,GPIO_DIRECTION_OUT);

    }

    public void rfidEnInit(){
        doExportGpio(GPIO_RFID_EN);
        doSetGpioDirection(GPIO_RFID_EN,GPIO_DIRECTION_OUT);
    }


    private final  native int export_gpio(int gpio);
    private final  native int set_gpio_direction(int gpio, int direction);
    private final  native int read_gpio_status(int gpio);
    private final  native int write_gpio_status(int gpio, int value);
    private final  native int unexport_gpio(int gpio);

    private  void  doExportGpio(int gpio){
        File device = new File("/sys/class/gpio/export");
        if ( checkDevFile(device)) {
            File gpioFile = new File("/sys/class/gpio/gpio" + gpio);
            if (gpioFile.exists())
                return;
            export_gpio(gpio);
        }
    }

    private  void doUnexportGpio(int gpio){
        File device = new File("/sys/class/gpio/unexport");
        if ( checkDevFile(device)) {
            File gpioFile = new File("/sys/class/gpio/gpio"+gpio);
            if (!gpioFile.exists())
                return;
            unexport_gpio(gpio);
        }

    }

    private  void doSetGpioDirection(int gpio, int direction){
        File device = new File("/sys/class/gpio/gpio"+gpio+"/direction");
        if ( checkDevFile(device)) {
            set_gpio_direction(gpio,direction);
        }

    }

    private  void doWriteGpioStatus(int gpio, int value){
        File device = new File("/sys/class/gpio/gpio"+gpio+"/value");
        if ( checkDevFile(device)) {
            write_gpio_status(gpio,value);
        }

    }

    private  int  doReadGpioStatus(int gpio){
        File device = new File("/sys/class/gpio/gpio"+gpio+"/value");
        if ( checkDevFile(device)) {
            return read_gpio_status(gpio);
        }
        return -1;
    }

    private  boolean checkDevFile(File device){
        if(device.exists()) {

//            if (!device.canRead() || !device.canWrite()) {
//                try {
//                    Process process = Runtime.getRuntime().exec("/system/xbin/su");
//                    DataOutputStream os = new DataOutputStream(process.getOutputStream());
//                    os.writeBytes("chmod 0777 " + device.getAbsolutePath() + "\n");
//                    os.writeBytes("exit" + "\n");
//                    os.close();
//
//                    if ((process.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
//                        throw new SecurityException();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    throw new SecurityException();
//                }
//            }

            return true;
        }
        return false;
    }

    private  void doSmdtSetExtGpioValue(int ioNum, int state){

        if (ioNum < 0 || ioNum > 7)
        {
            return;
        }
        String cmd = "busybox echo "
                + state
                + " > "
                + "/sys/class/gpio_xrm/gpio"
                + ioNum
                + "/data";
        executer(cmd);

    }

    private  int doSmdtGetExtGpioValue(int ioNum){

        if (ioNum < 0 || ioNum > 7)
        {
            return -1 ;
        }
        String cmd = "busybox cat "
                + "/sys/class/gpio_xrm/gpio"
                + ioNum
                + "/data";
        String ret = executer(cmd);
        return Integer.parseInt(ret);

    }

    private  String executer(String cmd) {

        StringBuffer output = new StringBuffer();
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = reader.readLine())!= null) {
                output.append(line + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (os != null)
                    os.close();
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (output.toString().equals(""))
        {
            return "";
        }
        String response = output.toString().trim().substring(0, output.length() - 1);
        return response;
    }

    static {
        System.loadLibrary("gpio_jni");
    }
}

