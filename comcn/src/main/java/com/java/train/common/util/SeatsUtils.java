package com.java.train.common.util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
public class SeatsUtils {



    static  String url="jdbc:mysql://localhost:3306/train_business?serverTimezone=Asia/Shanghai&useSSL=false";
    public static void main(String[] args) {
        String user="root";
        String password="123456";
        int carriageIndex=1;
        int row=8;
        int col=5;
        int seatType=2;
        int carriages=3;
        int cc=0;
        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            String sql="insert into train_seat(id,train_code,carriage_index,row,col,seat_type," +
                    "carriage_seat_index) values(?,?,?,?,?,?,?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            for (int count=1;count<=carriages;count++)
            {
                for(int i=1;i<=row;i++)
                {
                    for (int j=1;j<=col;j++)
                    {
                        cc++;
                        statement.setInt(1,80+(count-1)*40+cc);
                        statement.setString(2,"G1");
                        statement.setInt(3,count+1);
                        statement.setInt(4,i);
                        statement.setInt(5,j);
                        statement.setInt(6,seatType);
                        statement.setInt(7,cc);
                        statement.addBatch();

                    }
                }
                cc=0;
                statement.executeBatch();
                System.out.println("Data generation completed.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
