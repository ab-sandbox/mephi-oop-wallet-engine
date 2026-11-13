package ru.mephi.promoit.wallet.cli.command;

import java.nio.file.Path;

public record ExportCommand(Path path) implements FinanceCommand {
}
