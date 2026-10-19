package com.ctu.service;

import com.ctu.exception.PaymentFailedException;
import com.ctu.exception.RoomNotAvailableException;
import com.ctu.model.BookingReceipt;
import com.ctu.model.Room;
import com.ctu.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Unit Test Suite")
class BookingServiceTest {
    @Mock
    private RoomRepository roomRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private BookingService bookingService;

    private Room availableRoom;

    @BeforeEach
    void setUp() {
        availableRoom = new Room("R101", 500000, true);
    }

    @Test
    @DisplayName("Đặt phòng thành công: thanh toán, cập nhật trạng thái và lưu")
    void testBookRoom_Success() {
        when(roomRepository.findById("R101")).thenReturn(Optional.of(availableRoom));
        when(paymentGateway.charge("4111111111111111", 1000000)).thenReturn(true);

        BookingReceipt receipt = bookingService.bookRoom("R101", 2, "4111111111111111");

        assertNotNull(receipt);
        assertNotNull(receipt.getBookingId());
        assertFalse(receipt.getBookingId().isBlank());
        assertEquals("R101", receipt.getRoomId());
        assertEquals(1000000, receipt.getTotalAmount(), 0.001);
        assertFalse(availableRoom.isAvailable());
        verify(roomRepository, times(1)).findById("R101");
        verify(paymentGateway, times(1)).charge("4111111111111111", 1000000);
        verify(roomRepository, times(1)).save(availableRoom);
    }

    @Test
    @DisplayName("Không tìm thấy phòng")
    void testBookRoom_RoomNotFound_ThrowsException() {
        when(roomRepository.findById("R999")).thenReturn(Optional.empty());

        RoomNotAvailableException exception = assertThrows(RoomNotAvailableException.class,
                () -> bookingService.bookRoom("R999", 1, "4111111111111111"));

        assertEquals("Khong tim thay phong: R999", exception.getMessage());
        verify(paymentGateway, never()).charge(anyString(), anyDouble());
        verify(roomRepository, never()).save(any());
    }

    @Test
    @DisplayName("Phòng đã được đặt trước")
    void testBookRoom_RoomAlreadyBooked_ThrowsException() {
        availableRoom.setAvailable(false);
        when(roomRepository.findById("R101")).thenReturn(Optional.of(availableRoom));

        RoomNotAvailableException exception = assertThrows(RoomNotAvailableException.class,
                () -> bookingService.bookRoom("R101", 1, "4111111111111111"));

        assertEquals("Phong da duoc dat truoc do: R101", exception.getMessage());
        verifyNoInteractions(paymentGateway);
        verify(roomRepository, never()).save(any());
    }

    @Test
    @DisplayName("Thanh toán thất bại")
    void testBookRoom_PaymentFailed_ThrowsException() {
        when(roomRepository.findById("R101")).thenReturn(Optional.of(availableRoom));
        when(paymentGateway.charge("4111111111111111", 500000)).thenReturn(false);

        PaymentFailedException exception = assertThrows(PaymentFailedException.class,
                () -> bookingService.bookRoom("R101", 1, "4111111111111111"));

        assertEquals("Thanh toan qua the credit card that bai!", exception.getMessage());
        assertTrue(availableRoom.isAvailable());
        verify(paymentGateway).charge("4111111111111111", 500000);
        verify(roomRepository, never()).save(any());
    }

    @Test
    @DisplayName("Số đêm bằng 0 không hợp lệ")
    void testBookRoom_InvalidNights_Zero_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.bookRoom("R101", 0, "4111111111111111"));
        verifyNoInteractions(roomRepository, paymentGateway);
    }

    @Test
    @DisplayName("Số đêm âm không hợp lệ")
    void testBookRoom_InvalidNights_Negative_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.bookRoom("R101", -1, "4111111111111111"));
        verifyNoInteractions(roomRepository, paymentGateway);
    }

    @Test
    @DisplayName("Mã phòng null không hợp lệ")
    void testBookRoom_NullRoomId_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.bookRoom(null, 1, "4111111111111111"));
        verifyNoInteractions(roomRepository, paymentGateway);
    }

    @Test
    @DisplayName("Thẻ thanh toán null không hợp lệ")
    void testBookRoom_NullCreditCard_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.bookRoom("R101", 1, null));
        verifyNoInteractions(roomRepository, paymentGateway);
    }

    @Test
    @DisplayName("Thẻ thanh toán rỗng không hợp lệ")
    void testBookRoom_EmptyCreditCard_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.bookRoom("R101", 1, ""));
        verifyNoInteractions(roomRepository, paymentGateway);
    }

    @Test
    @DisplayName("Thẻ thanh toán chỉ chứa khoảng trắng không hợp lệ")
    void testBookRoom_BlankCreditCard_ThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> bookingService.bookRoom("R101", 1, "   "));
        verifyNoInteractions(roomRepository, paymentGateway);
    }
}
