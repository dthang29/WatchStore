package com.pet_project.watchstore.entity;

/** Không lưu trong DB, suy ra từ password_hash / google_id để hiển thị cho client. */
public enum AuthProvider {
    LOCAL,
    GOOGLE
}
