import java.time.*;
public class PayrollCalculator {
 public static double calculateWorkHours(LocalTime a,LocalTime b){if(!b.isAfter(a))throw new IllegalArgumentException();return Duration.between(a,b).toMinutes()/60.0;}
 public static double calculateOvertimeHours(double h){return Math.max(0,h-8);}
 public static int calculateLateMinutes(LocalTime a){return Math.max(0,(int)Duration.between(LocalTime.of(8,0),a).toMinutes());}
 public static double getHourlyRate(String l){return switch(l.toUpperCase()){case "JUNIOR"->20000.;case "MID"->30000.;case "SENIOR"->45000.;default->throw new IllegalArgumentException();};}
 public static double calculateOvertimePay(double h,double r){return h*1.25*r;}
 public static double calculateLateDeduction(int m,double r){return Math.floor(m/15.0)*.25*r;}
 public static long calculateNetPay(String l,LocalTime a,LocalTime b){double r=getHourlyRate(l),h=calculateWorkHours(a,b);return Math.round(Math.min(h,8)*r+calculateOvertimePay(calculateOvertimeHours(h),r)-calculateLateDeduction(calculateLateMinutes(a),r));}
 public static String formatPayslip(String n,String l,LocalTime a,LocalTime b){return n+" "+calculateNetPay(l,a,b);}
}
