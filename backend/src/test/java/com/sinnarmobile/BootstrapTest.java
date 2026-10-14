package com.sinnarmobile;

import com.sinnarmobile.Models.AdminUser;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class BootstrapTest {
    final AdminUsers admins=mock(AdminUsers.class);
    final Products products=mock(Products.class);
    final Categories categories=mock(Categories.class);
    final BCryptPasswordEncoder passwords=new BCryptPasswordEncoder(4);
    Bootstrap bootstrap() {
        var b=new Bootstrap(admins,products,categories,passwords);
        b.username="existingadmin";b.password="Test-new-password-123!";b.demo=false;return b;
    }
    AdminUser existing() {
        var u=new AdminUser();u.setUsername("existingadmin");u.setPasswordHash(passwords.encode("Old-test-password-123!"));
        when(admins.count()).thenReturn(1L);when(admins.findByUsername("existingadmin")).thenReturn(Optional.of(u));return u;
    }
    @Test void noResetWithoutExplicitRequest() {
        var u=existing();String original=u.getPasswordHash();bootstrap().run();
        assertThat(u.getPasswordHash()).isEqualTo(original);verify(admins,never()).save(any());
    }
    @Test void resetOnlyOnceForEachRequestId() {
        var u=existing();var b=bootstrap();b.resetId="recovery-1";b.run();
        assertThat(passwords.matches(b.password,u.getPasswordHash())).isTrue();assertThat(u.getPasswordResetId()).isEqualTo("recovery-1");
        String hash=u.getPasswordHash();b.password="Different-test-password-123!";b.run();
        assertThat(u.getPasswordHash()).isEqualTo(hash);verify(admins,times(1)).save(u);
        b.resetId="recovery-2";b.run();assertThat(passwords.matches(b.password,u.getPasswordHash())).isTrue();verify(admins,times(2)).save(u);
    }
    @Test void resetCannotCreateOrRenameAnAccount() {
        existing();var b=bootstrap();b.username="unknown";b.resetId="recovery-1";
        when(admins.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThatThrownBy(b::run).isInstanceOf(IllegalStateException.class);verify(admins,never()).save(any());
    }
    @Test void weakPasswordAndInvalidRequestIdRejected() {
        existing();var b=bootstrap();b.resetId="recovery-1";b.password="short";
        assertThatThrownBy(b::run).isInstanceOf(IllegalStateException.class);
        b.password="Valid-test-password-123!";b.resetId="invalid space";
        assertThatThrownBy(b::run).isInstanceOf(IllegalStateException.class);verify(admins,never()).save(any());
    }
}
