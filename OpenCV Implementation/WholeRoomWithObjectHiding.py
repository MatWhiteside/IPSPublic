import numpy as np
import cv2
import DetectorAPI

""" Initialise the camera with DirectShow API and set frame resolution """ 
def initCap():
    cap = cv2.VideoCapture(1+cv2.CAP_DSHOW)
    cap.set(cv2.CAP_PROP_FRAME_WIDTH, 1920)
    cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 1080)
    return cap

""" Draw a box around the detected person """
def drawBox(box):
    cv2.rectangle(img,(box[1],box[0]),(box[3],box[2]),(255,0,0),2)


""" Carry out a perspective transformation on the original image where points
    is a list = (TL, TR, BL, BR) """
def doPerspectiveTransformation(img, points, area_width, area_height):
    # Define dimensions for our perspective view window
    perspective_window_width = int(area_width*100)
    perspective_window_height = int(area_height*100)

    # Define source and destination points for perspective transformation
    pts1 = np.float32([list(points[0]), list(points[1]), list(points[2]), list(points[3])])
    pts2 = np.float32([[0, 0], [perspective_window_width, 0], [0, perspective_window_height], [perspective_window_width, perspective_window_height]])

    # Carry out perspective transformation
    matrix = cv2.getPerspectiveTransform(pts1, pts2)
    result = cv2.warpPerspective(img, matrix, (perspective_window_width, perspective_window_height))

    return result


""" Mask away all colours that aren't the detection dot we're looking for """
def applyMask(img):
    # Define two masks, putting them together and then applying it to our
    # perspective transformation output image
    result_hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
    # Lower mask (0-10)
    lower_red = np.array([0,200,200])
    upper_red = np.array([10,255,255])
    mask0 = cv2.inRange(result_hsv, lower_red, upper_red)
    # Upper mask (160-179)
    lower_red = np.array([160,200,200])
    upper_red = np.array([179, 255, 255])
    mask1 = cv2.inRange(result_hsv, lower_red, upper_red)
    # Combine masks and apply
    mask = mask0+mask1
    output_bgr = img.copy()
    output_bgr[np.where(mask==0)] = 0
    output_bgr[np.where(mask!=0)] = 255

    return output_bgr


if __name__ == "__main__":
    model_path = 'faster_rcnn_inception_v2_coco/frozen_inference_graph.pb'
    odapi = DetectorAPI.DetectorAPI(path_to_ckpt=model_path)
    threshold = 0.9
    cap = initCap()

    last_location = (0,0)

    # Averaging variables
    averageMax = 5
    valuesTaken = 0
    totalX = 0
    totalY = 0
    latestX = 0
    latestY = 0

    # Pixel locations of our virtual area
    top_left_coords = (320, 420)
    top_right_coords = (1090, 415)
    bottom_left_coords = (110, 685)
    bottom_right_coords = (1300, 685)

    behind_wardrobe_pts = np.array([(900, 100), (711, 293), (900, 293)])

    # Measurements of virtual area in ft
    area_width = 9
    area_height = 5

    room_map = cv2.imread('Map.png')  
    
    # Main program loop
    while True:
        # Capture the image and resize it for faster processing
        r, img = cap.read()
        img = cv2.resize(img, (1280, 720))

        # Use the DetectorAPI to find people in the frame
        boxes, scores, classes, num = odapi.processFrame(img)

        # Fetch all human boxes
        human_boxes = [x for x, y, z in zip(boxes, classes, scores) if y == 1 and z > threshold]

        # Visualize results
        for box in human_boxes:
            # Draw box around each human
            drawBox(box)

            # Draw the dot at the bottom of each box
            dotX = int(box[1]+(box[3]-box[1])/2)
            dotY = box[2]
            last_location = (dotX, dotY)
            cv2.circle(img, (dotX, dotY), 5, (0,0,250), -1)

            break

        # Detect if the user was around the wardrobe area before they
        # dissapeared
        was_in_box = False
        if(len(human_boxes) == 0):
            # Determine if the last known location was in the box near the wardrobe
            was_in_box = (last_location[0] > 900 and last_location[0] < 1100 and
                 last_location[1] > 400 and last_location[1] < 700)

        # Draw our virtual area
        cv2.circle(img, top_left_coords, 5, (255,0,0), -1)
        cv2.circle(img, top_right_coords, 5, (255,0,0), -1)
        cv2.circle(img, bottom_left_coords, 5, (255,0,0), -1)
        cv2.circle(img, bottom_right_coords, 5, (255,0,0), -1)

        # Carry out perspective transformation
        points = [top_left_coords, top_right_coords, bottom_left_coords, bottom_right_coords]
        transformation_result = doPerspectiveTransformation(img, points, area_width, area_height)

        # Mask the result so that we remove everything apart from the detection dot
        mask_result = applyMask(transformation_result)

        # Add the mask result onto the room map
        output_map = room_map.copy()

        # Infer the location if we can and the user has dissapeared, else draw
        # the detection dot
        if(was_in_box):
            cv2.drawContours(output_map, [behind_wardrobe_pts], 0, (0,0,0), -1)
        else:
            output_map[np.where(mask_result!=0)] = 0

        # Display results
        cv2.imshow("Original", img)
        cv2.imshow("Transformation", transformation_result)
        cv2.imshow("Masked", output_map)
        
        key = cv2.waitKey(1)
        if key & 0xFF == ord('q'):
            break

cap.release()
cv2.destroyAllWindows()

