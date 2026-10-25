import { z } from 'zod'

export const loginSchema = z.object({
  email: z.email('Email không hợp lệ'),
  password: z.string().min(1, 'Vui lòng nhập mật khẩu'),
})

export const registerSchema = z
  .object({
    fullName: z.string().trim().min(2, 'Họ tên tối thiểu 2 ký tự'),
    email: z.email('Email không hợp lệ'),
    password: z
      .string()
      .min(8, 'Mật khẩu tối thiểu 8 ký tự')
      .regex(/[A-Z]/, 'Cần ít nhất 1 chữ hoa')
      .regex(/[0-9]/, 'Cần ít nhất 1 chữ số'),
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: 'Mật khẩu xác nhận không khớp',
    path: ['confirmPassword'],
  })

export const verifyRegistrationSchema = z.object({
  email: z.email('Email không hợp lệ'),
  code: z.string().regex(/^\d{6}$/, 'Mã OTP phải gồm 6 chữ số'),
})

export const forgotPasswordSchema = z.object({
  email: z.email('Email không hợp lệ'),
})

export const resetPasswordSchema = z
  .object({
    email: z.email('Email không hợp lệ'),
    code: z.string().regex(/^\d{6}$/, 'Mã OTP phải gồm 6 chữ số'),
    newPassword: z.string()
      .min(8, 'Mật khẩu tối thiểu 8 ký tự')
      .max(72, 'Mật khẩu tối đa 72 ký tự')
      .regex(/[A-Z]/, 'Cần ít nhất 1 chữ hoa')
      .regex(/[0-9]/, 'Cần ít nhất 1 chữ số'),
    confirmPassword: z.string(),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: 'Mật khẩu xác nhận không khớp',
    path: ['confirmPassword'],
  })

export const profileSchema = z.object({
  fullName: z.string().trim().min(2, 'Họ tên tối thiểu 2 ký tự').max(150, 'Họ tên tối đa 150 ký tự'),
  phone: z
    .string()
    .trim()
    .regex(/^$|^(0|\+84)\d{9}$/, 'Số điện thoại không hợp lệ'),
  address: z.string().trim().max(255, 'Địa chỉ tối đa 255 ký tự'),
})
