package org.squiddev.plethora.core;

import com.google.common.base.Strings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import org.squiddev.plethora.api.PlethoraAPI;
import org.squiddev.plethora.core.docdump.WriteDocumentation;
import org.squiddev.plethora.utils.DebugLogger;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class CommandPlethora extends CommandBase {
	private final boolean restricted;

	public CommandPlethora(boolean restricted) {
		this.restricted = restricted;
	}

	@Override
	public String getCommandName() {
		return "plethora";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "dump|reload";
	}

	private String getCommandUsage(String type) {
		if (type.equals("dump")) {
			return "dump <name>";
		} else if (type.equals("reload")) {
			return "reload";
		} else {
			return "dump|reload";
		}
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (args.length < 1 || Strings.isNullOrEmpty(args[0])) throw new CommandException(getCommandUsage(sender));

		String type = args[0];
		if (type.equals("dump")) {
			if (args.length < 2 || Strings.isNullOrEmpty(args[1])) throw new CommandException(getCommandUsage(type));

			String name = args[1];
			try {
				OutputStream file = new FileOutputStream(name);

				try {
					PrintStream writer = new PrintStream(file);

					WriteDocumentation docs = new WriteDocumentation(writer);
					docs.writeHeader();
					docs.write(PlethoraAPI.instance().methodRegistry().getMethods());
					docs.writeFooter();
				} finally {
					file.close();
				}

				if (sender.sendCommandFeedback()) {
					sender.addChatMessage(new ChatComponentText("Documentation written to " + name));
				}
			} catch (Throwable e) {
				DebugLogger.error("Cannot handle " + name, e);
				throw new CommandException(e.toString());
			}
		} else if (type.equals("reload")) {
			ConfigCore.configuration.load();
			ConfigCore.sync();
		}
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender sender) {
		return !restricted || super.canCommandSenderUseCommand(sender);
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}
}