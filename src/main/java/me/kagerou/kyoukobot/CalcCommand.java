package me.kagerou.kyoukobot;

import de.btobastian.javacord.entities.message.Message;
import de.btobastian.sdcf4j.Command;
import de.btobastian.sdcf4j.CommandExecutor;
import kagerou.calculator.Calculator;
import kagerou.calculator.UnexpectedSymbolException;
//calculates an expression, the Calculator class is imported from Calculator.jar
public class CalcCommand implements CommandExecutor {
	@Command(aliases = {"k!calc"}, description = "Calculates an expression.", usage = "k!calc expr")
    public String onCommand(Message message, String[] args) {
		//String expr = message.getContent().substring(message.getContent().indexOf(' ') + 1);
		String expr = KyoukoBot.getArgument(message);
		String result;
		try {
			double res = (new Calculator()).eval(expr);
			//write the answer as long if it's close enough to a whole number
			if ((Math.abs(Math.round(res) - res) < 1e-10) && (Math.round(res) >= Long.MIN_VALUE) && ((Math.round(res) <= Long.MAX_VALUE)))
			{
				long long_res = (long) res;
				result = String.valueOf(long_res);
			}
			else
				result = String.valueOf(res);
		}
		catch (UnexpectedSymbolException e)
		{
			result = e.getMessage();
		}
		catch (ArithmeticException e)
		{
			result = "Arithmetic error.";
		}
		catch (StackOverflowError e)
		{
			result = "Stack overflow.";
		}
		return '`' +  result + '`';
	}
}
