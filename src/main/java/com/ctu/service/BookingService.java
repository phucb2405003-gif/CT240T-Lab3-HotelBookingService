package com.ctu.service;

import com.ctu.exception.PaymentFailedException;
import com.ctu.exception.RoomNotAvailableException;
import com.ctu.model.BookingReceipt;
import com.ctu.model.Room;
import com.ctu.repository.RoomRepository;
import java.util.UUID;

public class BookingService {
    private final RoomRepository roomRepository;
    private final PaymentGateway paymentGateway;

    public BookingService(RoomRepository roomRepository, PaymentGateway paymentGateway) {
        this.roomRepository = roomRepository;
        this.paymentGateway = paymentGateway;
    }

    /**
     * Đặt phòng khách sạn.
     * Điều kiện đầu vào: roomId khác null, nights > 0,
     * creditCard khác null và không rỗng.
     */
    public BookingReceipt bookRoom(String roomId, int nights, String creditCard) {
        // Kiểm tra tham số đầu vào.
        if (roomId == null || nights <= 0 || creditCard == null || creditCard.trim().isEmpty()) {
            throw new IllegalArgumentException("Tham so dat phong khong hop le!");
        }

        // Tìm phòng theo mã.
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotAvailableException("Khong tim thay phong: " + roomId));

        // Không cho đặt phòng đã được đặt.
        if (!room.isAvailable()) {
            throw new RoomNotAvailableException("Phong da duoc dat truoc do: " + roomId);
        }

        double totalAmount = room.getPricePerNight() * nights;

        // Thanh toán bằng cổng giả lập.
        boolean paymentSuccess = paymentGateway.charge(creditCard, totalAmount);
        if (!paymentSuccess) {
            throw new PaymentFailedException("Thanh toan qua the credit card that bai!");
        }

        // Chỉ cập nhật và lưu phòng khi thanh toán thành công.
        room.setAvailable(false);
        roomRepository.save(room);

        return new BookingReceipt(UUID.randomUUID().toString(), roomId, totalAmount);
    }
}
