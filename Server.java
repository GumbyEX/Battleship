/**
 * Created by Sam on 2019/10/28.
 */
import java.net.*;
import java.io.*;
import java.text.*;
import java.time.*;
import java.util.*;
public class Server implements Runnable
 {

  public static void main(String[] args)
   {
    Thread gameThread = new Thread(new Server());
    gameThread.start();
   }

    public void run()
     {
        try
         {

             String send = null;
             String take = null;
             ObjectInputStream in1 = null;
             ObjectInputStream in2 = null;
             ObjectOutputStream out1 = null;
             ObjectOutputStream out2 = null;
             Message message = new Message();

             ServerSocket server = new ServerSocket(5000);
             Socket client1 = server.accept();
             Socket client2 = server.accept();

             out1 = new ObjectOutputStream(client1.getOutputStream());
             in1 = new ObjectInputStream(client1.getInputStream());
             BattleShipTable fBoard1 = new BattleShipTable();
             BattleShipTable pBoard1 = new BattleShipTable();
             Message cli1 = new Message();
             Message cli2 = new Message();
             //fleet info
             //out1.writeByte(Message.MSG_REQUEST_INIT);
             cli1.setMsgType(Message.MSG_REQUEST_INIT);
             out1.writeObject(cli1);
             out1.reset();
             cli1 = (Message)in1.readObject();
             String[] spots = cli1.getMsg().split(" ");
             fBoard1.insertSubmarine(spots[0]);
             fBoard1.insertSubmarine(spots[1]);
             fBoard1.insertDestroyer(spots[2], spots[3]);
             fBoard1.insertDestroyer(spots[4], spots[5]);
             fBoard1.insertAirCarrier(spots[6], spots[7]);
             fBoard1.insertAirCarrier(spots[8], spots[9]);
             fBoard1.setupShips(spots);

             out2 = new ObjectOutputStream(client2.getOutputStream());
             in2 = new ObjectInputStream(client2.getInputStream());
             BattleShipTable fBoard2 = new BattleShipTable();
             BattleShipTable pBoard2 = new BattleShipTable();

             //fleet info
             cli2.setMsgType(Message.MSG_REQUEST_INIT);
             out2.writeObject(cli2);
             out2.reset();
             cli2 = (Message)in2.readObject();
             spots = cli2.getMsg().split(" ");
             fBoard2.insertSubmarine(spots[0]);
             fBoard2.insertSubmarine(spots[1]);
             fBoard2.insertDestroyer(spots[2], spots[3]);
             fBoard2.insertDestroyer(spots[4], spots[5]);
             fBoard2.insertAirCarrier(spots[6], spots[7]);
             fBoard2.insertAirCarrier(spots[8], spots[9]);
             fBoard2.setupShips(spots);


             cli1.Ftable = fBoard1;
             cli1.Ptable = pBoard1;
             cli2.Ftable = fBoard2;
             cli2.Ptable = pBoard2;
             String input = null;
             int[] spot;
             //actual gameplay
             while(true)
              {
                if(cli1.Ftable.sumOfHitcount() == 0 || cli2.Ftable.sumOfHitcount() == 0)
                 {
                     //end game
                    if(cli1.Ftable.sumOfHitcount() == 0)
                     {
                       cli1.setMsgType(Message.MSG_REQUEST_GAME_OVER);
                       cli2.setMsgType(Message.MSG_REQUEST_GAME_OVER);
                       cli1.setMsg("GAME OVER: You have lost.\n");
                       cli2.setMsg("GAME OVER: You have won!\n");
                       out1.writeObject(cli1);
                       out2.writeObject(cli2);
                       break;
                     }
                    else if(cli2.Ftable.sumOfHitcount() == 0)
                     {
                       cli1.setMsgType(Message.MSG_REQUEST_GAME_OVER);
                       cli2.setMsgType(Message.MSG_REQUEST_GAME_OVER);
                       cli1.setMsg("GAME OVER: You have won!\n");
                       cli2.setMsg("GAME OVER: You have lost.\n");
                       out1.writeObject(cli1);
                       out2.writeObject(cli2);
                       break;
                     }
                 }
                //Player one move
                cli1.setMsgType(Message.MSG_REQUEST_PLAY);
                cli1.setMsg(null);
                out1.writeObject(cli1);
                out1.reset();
                cli1 = (Message)in1.readObject();
                spot = fBoard1.AlphaNumerictoXY(cli1.getMsg());
                while(true)
                  {
                    if(cli1.Ptable.table[spot[0]][spot[1]].equals("Z"))
                     {
                       //if you haven't hit there before, then..
                       if(cli2.Ftable.table[spot[0]][spot[1]].equals("Z"))
                        {
                         //miss
                         cli1.Ptable.table[spot[0]][spot[1]] = "O";
                         cli2.Ftable.table[spot[0]][spot[1]] = "X";
                         cli1.setMsgType(Message.MSG_RESPONSE_PLAY);
                         cli1.setMsg("Miss");
                         out1.writeObject(cli1);
                         out1.reset();
                         break;
                        }
                       else
                        {
                         //hit something
                         cli1.Ptable.table[spot[0]][spot[1]] = "X";
                         cli2.Ftable.table[spot[0]][spot[1]] = "X";
                         cli1.setMsgType(Message.MSG_RESPONSE_PLAY);
                         cli1.setMsg("Hit");
                         out1.writeObject(cli1);
                         out1.reset();
                         input = cli2.Ftable.decrementCounter(fBoard2.XYToAlphaNumeric(spot));
                         if(input != null)
                          {
                           //if something was destroyed, tell the world about it
                           cli1.setMsgType(Message.MSG_SUNK);
                           cli1.setMsg("You sunk opponents " + input);
                           cli2.setMsgType(Message.MSG_SUNK);
                           cli2.setMsg("Your " + input + " was sunk");
                           out1.writeObject(cli1);
                           out1.reset();
                           out2.writeObject(cli2);
                           out2.reset();
                          }
                         break;
                        }
                     }
                    else
                     {
                      //spot already hit
                      cli1.setMsgType(Message.MSG_REQUEST_PLAY);
                      out1.writeObject(cli1);
                      out1.reset();
                      cli1 = (Message)in1.readObject();
                      spot = fBoard1.AlphaNumerictoXY(cli1.getMsg());
                    }
                  }
                 //check again if someone won
                 if(cli1.Ftable.sumOfHitcount() == 0 || cli2.Ftable.sumOfHitcount() == 0)
                  {
                   //end game
                   if(cli1.Ftable.sumOfHitcount() == 0)
                    {
                     cli1.setMsgType(Message.MSG_REQUEST_GAME_OVER);
                     cli2.setMsgType(Message.MSG_REQUEST_GAME_OVER);
                     cli1.setMsg("GAME OVER: You have lost.\n");
                     cli2.setMsg("GAME OVER: You have won!\n");
                     out1.writeObject(cli1);
                     out2.writeObject(cli2);
                     break;
                    }
                   else if(cli2.Ftable.sumOfHitcount() == 0)
                    {
                     cli1.setMsgType(Message.MSG_REQUEST_GAME_OVER);
                     cli2.setMsgType(Message.MSG_REQUEST_GAME_OVER);
                     cli1.setMsg("GAME OVER: You have won!\n");
                     cli2.setMsg("GAME OVER: You have lost.\n");
                     out1.writeObject(cli1);
                     out2.writeObject(cli2);
                     break;
                    }
                  }
                 //Player 2 move
                 cli2.setMsgType(Message.MSG_REQUEST_PLAY);
                 cli2.setMsg(null);
                 out2.writeObject(cli2);
                 out2.reset();
                 cli2 = (Message)in2.readObject();
                 spot = fBoard2.AlphaNumerictoXY(cli2.getMsg());
                 while(true)
                  {
                    if(cli2.Ptable.table[spot[0]][spot[1]].equals("Z"))
                     {
                      //if you haven't hit there before, then..
                      if(cli1.Ftable.table[spot[0]][spot[1]].equals("Z"))
                       {
                        //miss
                        cli2.Ptable.table[spot[0]][spot[1]] = "O";
                        cli1.Ftable.table[spot[0]][spot[1]] = "X";
                        cli2.setMsgType(Message.MSG_RESPONSE_PLAY);
                        cli2.setMsg("Miss");
                        out2.writeObject(cli2);
                        out2.reset();
                        break;
                        }
                       else
                        {
                         //hit something
                         cli2.Ptable.table[spot[0]][spot[1]] = "X";
                         cli1.Ftable.table[spot[0]][spot[1]] = "X";
                         cli2.setMsgType(Message.MSG_RESPONSE_PLAY);
                         cli2.setMsg("Hit");
                         out2.writeObject(cli2);
                         out2.reset();
                         input = cli1.Ftable.decrementCounter(fBoard2.XYToAlphaNumeric(spot));
                         if(input != null)
                          {
                           //if something was destroyed, tell the world about it
                           cli2.setMsgType(Message.MSG_SUNK);
                           cli2.setMsg("You sunk opponents " + input);
                           cli1.setMsgType(Message.MSG_SUNK);
                           cli1.setMsg("Your " + input + " was sunk");
                           out2.writeObject(cli2);
                           out2.reset();
                           out1.writeObject(cli1);
                           out1.reset();
                          }
                         break;
                        }
                      }
                     else
                      {
                       //spot already hit, just repeat request
                       cli2.setMsgType(Message.MSG_REQUEST_PLAY);
                       out2.writeObject(cli2);
                       cli2 = (Message)in2.readObject();
                       spot = fBoard2.AlphaNumerictoXY(cli2.getMsg());
                      }
                  }
              }
             out1.close();
             out2.close();
             in1.close();
             in2.close();
             client1.close();
             client2.close();
             server.close();
             System.exit(1);
         }
        catch(IOException e)
         {
          System.out.println("Something IO related went wrong.");
         }
        catch(ClassNotFoundException e)
         {
          System.out.println("Check your classes.");
         }
     }
 }
