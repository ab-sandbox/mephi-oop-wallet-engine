package ru.mephi.promoit.wallet.persistence.dto;

import java.util.List;

public record ApplicationData(
    List<UserData> users,
    List<UserFinanceData> finances
) {
}
