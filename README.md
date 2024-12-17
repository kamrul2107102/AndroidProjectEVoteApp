<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Simple E-Vote App</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            margin: 0;
            padding: 20px;
        }

        h1 {
            color: #333;
        }

        h2 {
            color: #4CAF50;
        }

        p {
            font-size: 16px;
            color: #555;
            line-height: 1.5;
        }

        code {
            background-color: #f4f4f4;
            padding: 5px 10px;
            border-radius: 4px;
            color: #333;
        }

        pre {
            background-color: #2e2e2e;
            color: #fff;
            padding: 15px;
            border-radius: 4px;
            overflow-x: auto;
        }

        .step {
            background-color: #fff;
            padding: 15px;
            margin: 10px 0;
            border-radius: 4px;
            box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
        }

        .footer {
            background-color: #333;
            color: white;
            text-align: center;
            padding: 10px;
            position: fixed;
            bottom: 0;
            width: 100%;
        }
    </style>
</head>
<body>

    <h1>Simple E-Vote App</h1>
    <p>This is a simple e-vote app that integrates Firebase Authentication, Firebase Realtime Database, and JSON parsing to manage user login, registration, and vote storage.</p>

    <h2>Features</h2>
    <ul>
        <li><strong>Firebase Authentication</strong>: Secure user login and registration system using Firebase.</li>
        <li><strong>Firebase Realtime Database</strong>: Real-time database for storing and retrieving user data and votes.</li>
        <li><strong>JSON Parsing</strong>: Efficient parsing and handling of JSON data for communication with external APIs or databases.</li>
    </ul>

    <h2>Prerequisites</h2>
    <p>Before running the app, make sure you have the following:</p>
    <ul>
        <li><strong>Android Studio</strong>: Installed on your machine for Android development.</li>
        <li><strong>Firebase Account</strong>: Set up Firebase and link it to your Android app for authentication and database functionality.</li>
        <li><strong>Basic Knowledge</strong>: Familiarity with Android development, Firebase, and JSON parsing.</li>
    </ul>

    <h2>Setup Instructions</h2>
    <div class="step">
        <h3>Step 1: Clone the Repository</h3>
        <p>Clone the repository to your local machine using the following command:</p>
        <pre><code>git clone https://github.com/your-username/e-vote-app.git</code></pre>
    </div>

    <div class="step">
        <h3>Step 2: Set Up Firebase</h3>
        <p>1. Go to the <a href="https://console.firebase.google.com/" target="_blank">Firebase Console</a>.</p>
        <p>2. Create a new project and add an Android app.</p>
        <p>3. Download the <strong>google-services.json</strong> file and add it to the <code>app</code> folder in your project.</p>
        <p>4. Enable <strong>Firebase Authentication</strong> and <strong>Realtime Database</strong> in the Firebase console.</p>
    </div>

    <div class="step">
        <h3>Step 3: Install Dependencies</h3>
        <p>Ensure that your <code>build.gradle</code> files are configured with the necessary Firebase dependencies. In your <code>app/build.gradle</code> file, add the following:</p>
        <pre><code>dependencies {
    implementation 'com.google.firebase:firebase-auth:21.0.1'
    implementation 'com.google.firebase:firebase-database:20.0.3'
    implementation 'com.google.code.gson:gson:2.8.8'
}</code></pre>
        <p>Sync your project with Gradle files.</p>
    </div>

    <div class="step">
        <h3>Step 4: Run the App</h3>
        <p>1. Open Android Studio and load the project.</p>
        <p>2. Connect an Android device or start an emulator.</p>
        <p>3. Run the app on your device/emulator.</p>
    </div>

    <h2>Usage</h2>
    <p>Users can sign up or log in using their Firebase credentials. After authentication, they can vote, and their vote is stored in the Firebase Realtime Database. The app retrieves and displays votes using JSON parsing.</p>

    <h2>License</h2>
    <p>This project is licensed under the MIT License - see the <a href="LICENSE" target="_blank">LICENSE</a> file for details.</p>

    <h2>Contributing</h2>
    <p>If you have any suggestions or improvements, feel free to fork this repository and submit a pull request.</p>

    <h2>Acknowledgments</h2>
    <p>Special thanks to the <a href="https://github.com/Dilan012/EasyVote-Android-App/blob/master/build.gradle" target="_blank">EasyVote Android App</a> for the inspiration and guidance on building the app.</p>

    <div class="footer">
        <p>For any questions or feedback, feel free to contact me at <a href="mailto:your-email@example.com" style="color: white;">your-email@example.com</a></p>
    </div>

</body>
</html>
