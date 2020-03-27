/**
 * Created by Sam on 2019/10/28.
 */
import java.io.*;
import java.net.*;
import java.util.Scanner;
public class Client
 {
     public static void main(String[] args) throws Exception
      {
          Socket clientSocket = null;
          ObjectOutputStream outToServer = null;
          ObjectInputStream inFromServer = null;

          clientSocket = new Socket("127.0.0.1", 5000);
          outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
          inFromServer = new ObjectInputStream(clientSocket.getInputStream());
          Message message = null;

          int inputInt;
          message = (Message)inFromServer.readObject();
          String input = null;
          Scanner kb = new Scanner(System.in);
          String sendingString = "";
          if(message.getMsgType() == 1)
           {
             System.out.println("Please follow the following instructions to set up the board for play.");
             // to do:, for now its an example board
             int counter = 0;
             while(counter < 6)
              {
               if(counter < 2)
                {
                 //Subs
                 while(counter < 2)
                  {
                   System.out.println("Please enter the space in which you wish to place a submarine.");
                   input = kb.nextLine();
                   if(isValid(input))
                    {
                     counter++;
                     sendingString += input;
                     sendingString += " ";
                    }
                   else
                    {
                     System.out.println("That wasn't a correct board position.");
                    }
                  }
                }
               else if(counter < 4 && counter >= 2)
                {
                //Destroyers
                while(counter < 4)
                 {
                  System.out.println("Please enter the first 2 coords in which you wish to place a destroyer.");
                  input = kb.nextLine();
                  if(isValid2Spaces(input))
                   {
                    counter++;
                    sendingString += input;
                    sendingString += " ";
                   }
                  else
                   {
                    System.out.println("One of your coordinates wasn't a correct board position.");
                   }
                 }
                }
               else
                {
                 //Aircraft Carrier
                 while(counter < 6)
                  {
                   System.out.println("Please enter the first 2 coords you wish to have the Aircraft Carrier occupy.");
                   input = kb.nextLine();
                   if(isValid2Spaces(input))
                    {
                     counter++;
                     sendingString += input;
                     sendingString += " ";
                    }
                   else
                    {
                     System.out.println("One of your coordinates wasn't a correct board position.");
                    }
                  }
                }
              }
             message.setMsg(sendingString);
             outToServer.writeObject(message);
             outToServer.reset();
             // Start message implementation
             String move = null;
             while(true)
              {
               message = (Message)inFromServer.readObject();
               if(message.getMsgType() == 3)
                {
                 //Make a move
                 System.out.println("Your Board:\n");
                 System.out.println(message.Ftable.toString());
                 System.out.println("Your Hits:\n");
                 System.out.println(message.Ptable.toString());
                 System.out.println("Choose a spot to hit.");
                 while(true)
                  {
                   move = kb.nextLine();
                   if(isValid(move))
                       break;
                   else
                       System.out.println("Invalid board space.");
                  }
                 message.setMsg(move);
                 outToServer.writeObject(message);
                 outToServer.reset();
                }
               else if(message.getMsgType() == 4)
                {
                 //spits out hit or miss
                 System.out.println(message.getMsg());
                }
               else if(message.getMsgType() == 6)
                {
                 //if something is sunk, say something
                 System.out.println(message.getMsg());
                }
               else if(message.getMsgType() == 5)
                {
                 //if game is over, print who one and end it
                 System.out.println(message.getMsg());
                 break;
                }
               //message = (Message)inFromServer.readObject();
              }
           }
          inFromServer.close();
          outToServer.close();
          System.exit(1);
      }
     public static boolean isValid(String input)
      {
       int []ret = new int[2];
       ret[0] = ((int)input.charAt(0) - (int)'A');
       if(input.substring(1).equals(""))
           return false;
       ret[1] = Integer.parseInt(input.substring(1));
       if(ret[0] >= 10 || ret[0] < 0)
           return false;
       if(ret[1] >=10 || ret[1] < 0)
           return false;
       return true;
      }
     public static boolean isValid2Spaces(String input)
      {
       String[] split = input.split(" ");
       if(split.length != 2)
           return false;
       int[] ret1 = new int[2];
       int[] ret2 = new int[2];
       ret1[0] = ((int)split[0].charAt(0) - (int)'A');
       ret1[1] = Integer.parseInt(split[0].substring(1));
       ret2[0] = ((int)split[1].charAt(0) - (int)'A');
       ret2[1] = Integer.parseInt(split[1].substring(1));
       if(ret2[0] >= 10 || ret1[0] < 0)
           return false;
       if(ret2[1] >=10 || ret1[1] < 0)
           return false;
       if(ret2[0] >= 10 || ret2[0] < 0)
           return false;
       if(ret2[1] >=10 || ret2[1] < 0)
           return false;
       return true;
      }
 }
