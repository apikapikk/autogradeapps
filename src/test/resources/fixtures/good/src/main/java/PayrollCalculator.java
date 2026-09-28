import java.time.*;
public class PayrollCalculator {
  private static final double JUNIOR_RATE=20000, MID_RATE=30000, SENIOR_RATE=45000;
  public static double calculateWorkHours(LocalTime in,LocalTime out){if(!out.isAfter(in))throw new IllegalArgumentException();return Duration.between(in,out).toMinutes()/60.0;}
  public static double calculateOvertimeHours(double h){return Math.max(0,h-8);}
  public static int calculateLateMinutes(LocalTime in){return Math.max(0,(int)Duration.between(LocalTime.of(8,0),in).toMinutes());}
  public static double getHourlyRate(String l){return switch(l.toUpperCase()){case "JUNIOR"->JUNIOR_RATE;case "MID"->MID_RATE;case "SENIOR"->SENIOR_RATE;default->throw new IllegalArgumentException();};}
  public static double calculateOvertimePay(double h,double r){return Math.min(h,2)*1.5*r+Math.max(0,h-2)*2*r;}
  public static double calculateLateDeduction(int m,double r){return Math.ceil(m/15.0)*.25*r;}
  public static long calculateNetPay(String l,LocalTime in,LocalTime out){double r=getHourlyRate(l),h=calculateWorkHours(in,out),o=calculateOvertimeHours(h);return Math.max(0,Math.round(Math.min(h,8)*r+calculateOvertimePay(o,r)-calculateLateDeduction(calculateLateMinutes(in),r)));}
  public static String formatPayslip(String n,String l,LocalTime in,LocalTime out){return n+" | "+l+" | Net pay: "+calculateNetPay(l,in,out);}
}
