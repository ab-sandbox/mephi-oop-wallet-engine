package ru.mephi.promoit.wallet.persistence.dto;

import java.util.UUID;

public record UserData(
    UUID id,
    String login,
    String passwordHash
) {
}
