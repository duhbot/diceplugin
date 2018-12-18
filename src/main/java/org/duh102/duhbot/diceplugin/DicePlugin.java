package org.duh102.duhbot.diceplugin;

import java.util.*;
import java.util.regex.*;

import org.pircbotx.hooks.*;
import org.pircbotx.hooks.types.*;
import org.pircbotx.hooks.events.*;

import org.duh102.duhbot.functions.*;
public class DicePlugin extends ListenerAdapter implements ListeningPlugin
{
  static Random random = null;
  static Pattern diceCommandPattern = Pattern.compile("^!dice\\s([0-9]+)d([0-9]+)([+-]([0-9]+))?");
  static Pattern diceImplicitPattern = Pattern.compile("^!dice\\s([0-9]+)(d([0-9]+)){2,10}");
  static Pattern diceImplicitPatternPart = Pattern.compile("d([0-9]+)");
  public static final int PUBLIC_DICE_LIMIT = 10, PRIVATE_DICE_LIMIT = 20;
  
  public DicePlugin()
  {
    if(random == null)
    {
      random = new Random();
    }
  }
  static String message;
  public void onMessage(MessageEvent event)
  {
    diceRoll(event, PUBLIC_DICE_LIMIT);
  }
  public void onPrivateMessage(PrivateMessageEvent event)
  {
    diceRoll(event, PRIVATE_DICE_LIMIT);
  }
  
  private void diceRoll(GenericMessageEvent event, int diceLimit)
  {
    message = org.pircbotx.Colors.removeFormattingAndColors(event.getMessage()).trim();
    Matcher implicitMatch = diceImplicitPattern.matcher(message);
    Matcher commandMatch = diceCommandPattern.matcher(message);
    if(implicitMatch.matches())
    {
      int numDice = 0;
      int numSides = 1;
      int rollResult = 0;
      try
      {
        numDice = Math.max(Integer.parseInt(implicitMatch.group(1)),1);
        Matcher implicitMatchGroups = diceImplicitPatternPart.matcher(message);
        
        while(implicitMatchGroups.find())
        {
          
          numSides = Math.max(Integer.parseInt(implicitMatchGroups.group(1)), 2);
          rollResult = (random.nextInt(numSides) + 1);
          numDice *= rollResult;
        }
        numDice /= rollResult;
        int accumulator = 0;
        for(int i = 0; i < numDice; i++)
        {
          accumulator += (random.nextInt(numSides) + 1);
        }
        event.respond("" + accumulator);
      }
      catch(NumberFormatException nfe)
      {
        nfe.printStackTrace();
      }
      catch(ArithmeticException ae)
      {
        ae.printStackTrace();
      }
    }
    else if(commandMatch.matches())
    {
      int numDice = 0, diceSides = 0, extra = 0;
      try
      {
        numDice = Math.min(Math.max(Integer.parseInt(commandMatch.group(1)), 1), diceLimit);
        diceSides = Math.min(Math.max(Integer.parseInt(commandMatch.group(2)), 2), 2147483647);
        if(commandMatch.group(3) != null)
        {
          extra = Integer.parseInt(commandMatch.group(3));
          int accumulator = extra, rand = 0;;
          StringBuilder diceList = new StringBuilder(String.format("%dd%d%+d: ", numDice, diceSides, extra));
          for(int i = 0; i < numDice; i++)
          {
            rand = (random.nextInt(diceSides) + 1);
            accumulator += rand;
            diceList.append(rand + ", ");
          }
          diceList.append(String.format("%+d", extra));
          event.respond(String.format("%s; %d", diceList.toString(), accumulator));
        }
        else
        {
          StringBuilder diceList = new StringBuilder(numDice+"d"+diceSides+": " + (random.nextInt(diceSides) + 1));
          for(int i = 1; i < numDice; i++)
          {
            diceList.append(", " + (random.nextInt(diceSides) + 1));
          }
          event.respond(diceList.toString());
        }
      }
      catch(NumberFormatException nfe)
      {
        nfe.printStackTrace();
      }
    }
  }
  
  public HashMap<String,String> getHelpFunctions()
  {
    HashMap<String,String> helpFunctions = new HashMap<String,String>();
    helpFunctions.put("!dice xdy([+-]z)", "Rolls x y-sided die and returns the result, optionally adding or subtracting z from the total. Limits: [1,10] dice and [2, 2147483647] sides, z a valid integer");
    helpFunctions.put("!dice xdy(dz...)du", "Rolls xdy(dz...) du-sided die and returns the result. Limits: [2,10] d segments and [2, 2147483647] final result");
    return helpFunctions;
  }
  
  public String getPluginName()
  {
    return "Dice";
  }
  
  public ListenerAdapter getAdapter()
  {
    return this;
  }
}
