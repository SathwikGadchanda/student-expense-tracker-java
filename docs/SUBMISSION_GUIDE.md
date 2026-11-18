# Submission and sharing

## Files to submit

| College requirement | Deliverable | Status |
| --- | --- | --- |
| PPT Google Drive link | Student_Expense_Tracker_Presentation.pptx | File prepared. Upload and share as below. |
| GitHub link | Public student-expense-tracker-java repository | Publication requires an accessible repository. Do not submit an unverified URL. |
| Live/deployed link, if any | Desktop Java application | Not applicable. Share a demonstration recording instead. |
| Project report | Student_Expense_Tracker_Report.pdf | File prepared. Fill academic placeholders before final submission. |

The certificate is a placeholder only. Enter your actual college name, student name, roll number, department and supervisor. Ask your institution to complete any required certificate or signature. Nothing in the report claims approval.

## Google Drive upload and public viewer links

Google Drive was not connected during preparation. No Drive upload or public sharing was performed.

1. Open `https://drive.google.com` and sign into the Google account used for submission.
2. Select **New > Folder** and name it **Student Expense Tracker Submission**.
3. Open the folder. Select **New > File upload** and upload the PowerPoint and PDF report.
4. Right-click the PowerPoint and choose **Share > Share**.
5. Under **General access**, change **Restricted** to **Anyone with the link**, and choose **Viewer**.
6. Select **Copy link**, then **Done**. Paste that link into the college's PPT Link field.
7. Repeat the same steps for the PDF report if your college needs a report URL.
8. Test each copied link in an incognito window. If the account prevents public sharing, use your college's permitted sharing option or ask its administrator.

Keep the original PPTX when uploading. Opening it in Google Slides is optional. If converting to Google Slides, review diagrams and screenshot proportions before sharing the converted file.

## GitHub publication

The connected GitHub account is SathwikGadchanda. The available connector supports file and commit modifications, but does not expose repository creation. The checked browser session required sign-in.

Create a public repository named `student-expense-tracker-java` at `https://github.com/new`, with a README initialized. After this, the connected GitHub tool can upload the prepared source. Alternatively, use Git locally in the extracted source directory:

```powershell
git init -b main
git add .
git commit -m "Build Student Expense Tracker with SQLite and tests"
git remote add origin https://github.com/YOUR_USERNAME/student-expense-tracker-java.git
git push -u origin main
```

For this local Git route, create an empty remote repository without README, so there is no competing initial history. Replace YOUR_USERNAME with your actual GitHub username. If you already created a README on the remote, clone that repository first, copy the prepared source into it, commit and push. Use Git Credential Manager's normal browser sign-in. Never paste a token into source files, README or screenshots. Check the public repository page in an incognito window after pushing.

The source ZIP excludes personal databases, CSV exports, downloaded JDK/Maven tooling and private account data. Screenshots use synthetic demonstration records.

## Demo video

Record the application for about 2-3 minutes using Windows 11 Snipping Tool's Record feature, or Xbox Game Bar with **Win+G**. Show only the app, use demo data and read the script in MENTOR_GUIDE.md. Review the recording, upload the MP4 to Drive and set **Anyone with the link > Viewer** if permitted. Use that recording URL as a demo link only when your college accepts a video instead of a hosted app.

Do not present a GitHub repository URL as a live deployed application. Swing requires a desktop Java runtime and is not a browser-hosted website.

## Final checklist

- [ ] Replace the report's personal and academic placeholders.
- [ ] Run `java -jar student-expense-tracker.jar` on the presentation computer.
- [ ] Confirm the report and the 12-slide PPT open correctly.
- [ ] Upload the PPT to Drive, set Viewer sharing and test the copied link.
- [ ] Upload the report and test its link if a link is required.
- [ ] Verify the GitHub repository is public and contains source, pom.xml, docs and screenshots.
- [ ] Record a short demo and share the video if needed.
- [ ] Practise the 20 viva questions and explain the actual classes.
- [ ] Submit only links that you have opened successfully.
