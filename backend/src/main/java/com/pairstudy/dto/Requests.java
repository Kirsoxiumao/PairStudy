package com.pairstudy.dto;
import jakarta.validation.constraints.*;
import java.util.List;
public final class Requests {
 private Requests() {}
 public record Register(@NotBlank @Pattern(regexp="[A-Za-z0-9_]{3,32}") String username,
   @NotBlank @Size(min=8,max=64) String password, @NotBlank @Size(max=40) String nickname) {}
 public record Login(@NotBlank @Size(max=32) String username, @NotBlank @Size(max=64) String password) {}
 public record Bind(@NotBlank @Pattern(regexp="[A-Fa-f0-9]{16}") String inviteCode) {}
 public record CategoryInput(@NotBlank @Size(max=40) String name, @NotBlank @Size(max=32) String iconName,
   @Min(0) @Max(100000) int sortOrder) {}
 public record CheckinInput(@NotNull @Positive Long categoryId, @NotNull @Size(max=5000) String content,
   @NotNull @Size(max=9) List<@NotNull @Positive Long> imageIds,
   @NotBlank @Pattern(regexp="[a-fA-F0-9-]{36}") String requestId) {}
}
