export class TableModel {
  constructor(data) {
    this.id = data.id;
    this.numberTable = data.numberTable || "Unamed Table";
    this.capacity = data.capacity || 0;
    this.status = data.status;

    this.isAvailable = this.status === "AVAILABLE";
    this.statusText = this.status === "AVAILABLE" ? "Trống" : "Đang sử dụng";

    this.qrCodeImage = data.qrCode
      ? `data:image/png;base64,${data.qrCode}`
      : "https://placehold.co/150?text=No+QR";

    this.createdAt = data.createdAt;
  }
}
