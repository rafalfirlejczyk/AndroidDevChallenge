# AndroidDevChallenge
I am going to participate in this challenge and use machine learning on the android device. I will follow challenge rules defined here:
https://developer.android.com/dev-challenge

# My idea is:

to start simple :-)
For many people and for some professions, the quality of the pictures taken is crucial. Sometimes pictures are taken in a hurry and in poor lighting conditions. Evaluating the quality of a photo using a small smartphone screen can be difficult and insufficient. This can lead to poor-quality photos and, ultimately, a missed opportunity to capture something unique, or simply the need to return and re-make them. Sometimes this is no longer possible and all work is lost. This may apply not only to tourists who will not be able to boast of their next great quality photos, but also to people whose work depends on a well-made photo. It is not only about journalists, but also all who document their work with photos, such as parcel suppliers, who in the absence of the addressee should take a photo of the delivered package and its label, policemen documenting the scene of the accident or crime, insurance agents documenting damage, dermatologists photographing patients' skin lesions. They can also be farmers who, for the sake of their crops, photograph the leaves of cultivated plants to discover an approaching disease or pest attack early enough to apply appropriate protection. The quality of the pictures taken is fundamental. A solution to these problems could be an application that would assess the quality of the photo on an ongoing basis and give the shooter a hint whether it is good enough or should be repeated.

# Bringing my plan to life.

The application classifying the quality of "just taken" image should run on Android smartphones. It should implement Machine Learning model and run the inference direct on the devices ("in the edge"). It should use TFLite model and implement Android NN API (if possible) There are several steps in this project to be made:
1. Concept. 
    Discussion how to qualify good or bad quality image. 
    Definition of possible classes. Three classes chosen. Good Quality Image, Noisy Image and Blurred Image
2. Selecting training data.
For training data I chose 1162 good quality images from imagenet. Its is almost not possible to find the same number of blurred or noisy images because nobody likes to save them.  :-) To overcome this problem I processed the good quality images and add noise and blur using software (matlab). Having done this I got 3 classes of 1162 images prepared to serve as training data for my model
3. Selecting model.
1162 images per class seems to be many but it is still not enough to train a deep convolutional network from the scratch. I decided to go for pretrained model and retrain it to my images. Important factor in decision making was the fact, that Google open sourced some good quality models and made pretrained, freezed models. After several trials I decided to chose well known inception_v3 model. Very helpfull in advancing the project was the tutorial "Tensorflow for poets" written by Pete Warden from Google. The pretrained inception_v3 model was retrained using my images. As an outcome I got the .pb retrained model.
4. Model conversion. In order to fit the model into limited memory of the smartphone and to get fast inference the model has to be simplified. In the beggining the idea was to convert it into TFLite model and run it on the smaprtphone equiped with Qualcomm Snapdragon SD660 using Android NN APIs. However last year there was no support of Android NN APIs on SD660. The decision was taken to use proprietary Qualcomm SNPE API which was available and convert it to Qualcomm specific dlc and dlc_quantized model. SNPE also allows to utilise not only CPU but also GPU or even DSP for running the inferfence giving also a possibility to check the performance (speed and energy consumption) of different components when running ML on the device. Results can be seen here:
https://medium.com/@firlejczyk/computer-vision-application-on-mobile-device-392ad3a6b26a
5. Running inference on device.
For the simple test on the device I used the qualcomm demo application published in the Qualcomm tutorial:
https://developer.qualcomm.com/docs/snpe/android_tutorial.html
5. Results
Achieved accuracy was about 90-92%. Inference time on CPU took about 1s, on GPU about 300ms and on DSP about 60ms. 
8. Steps to be done until May 2020.
I. Converting the model to TFLite to become HW agnostic
II. Developping an Android application using Android NN API (not SNPE from Qualcomm). The app should run inference on camera image (jus taken) and classify it according the calculated class probability.





