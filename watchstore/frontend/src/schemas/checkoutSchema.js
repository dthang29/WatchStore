import { z } from 'zod'

export const checkoutSchema = z.object({
  receiverName: z.string().trim().min(2, 'Vui lòng nhập tên người nhận'),
  phone: z.string().regex(/^(0|\+84)\d{9}$/, 'Số điện thoại không hợp lệ'),
  address: z.string().trim().min(10, 'Vui lòng nhập địa chỉ đầy đủ'),
  note: z.string().max(500).optional(),
  paymentMethod: z.enum(['COD', 'BANK_TRANSFER', 'VNPAY']),
})
