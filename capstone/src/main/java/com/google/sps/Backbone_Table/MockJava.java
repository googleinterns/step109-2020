import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/* 
    This is a mock java that will serves as a guide for how to get data from a 
    CSV file and but that data in a table in SQL
*/
public class MockJava{
    public static void main(String[] args) throws FileNotFoundException{    
        String path = "Mock_University_Table.csv";
        if(!isValidPath(path)){
            System.out.println("Not Valid Path!");
            return;
        }
        File data = new File(path);
        Scanner dataScan = new Scanner(data);

        if(dataScan.hasNext()){
            dataScan.nextLine();
        }
        while (dataScan.hasNext()){
            String curLine = dataScan.nextLine();
            if (!curLine.contains(",")){
                System.out.println("Line does not have comma");
            }
            else{
                // This will be replaced by a method that will update the 
                // University table with the university name and state
                String University = curLine.substring(0,curLine.lastIndexOf(","));
                System.out.println(University);
                String State = curLine.substring(curLine.lastIndexOf(",")+1);
                System.out.println(State);
            }
        }

    }

    public static boolean isValidPath(String path){
        File data = new File(path);
        return data.exists();
    } 
}   