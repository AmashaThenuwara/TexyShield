from ultralytics import YOLO

model = YOLO("yolov8n.pt")

model.train(
    data="ppe_dataset/data.yaml",
    epochs=50,
    imgsz=640,
    batch=8
)
