
'use strict';

// [START import]
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp()
const spawn = require('child-process-promise').spawn;
const path = require('path');
const zlib = require('zlib');
const os = require('os');
const fs = require('fs');
const crypto = require('crypto');
const {Storage} = require('@google-cloud/storage');

const { Transform } = require('stream');
exports.ALGORITHM = 'AES-256-CBC';
exports.ENCRYPED_EXT = '.enc';
exports.UNENCRYPED_EXT = '.unenc';



// [END import]


class AppendInitVect extends Transform {
  constructor(initVect, opts) {
    super(opts);
    this.initVect = initVect;
    this.appended = false;
  }

  _transform(chunk, encoding, cb) {
    if (!this.appended) {
      this.push(this.initVect);
      this.appended = true;
    }
    this.push(chunk);
    cb();
  }
}

function getCipherKey(key) {
  return crypto.createHash('sha256').update(key).digest();
}

function encrypt({ file, password }) {
  console.log("call success"+file+password);
 // Generate a secure, pseudo random initialization vector.
  const initVect = crypto.randomBytes(16);
  
  // Generate a cipher key from the password.
  const CIPHER_KEY = getCipherKey(password);
  const readStream = fs.createReadStream(file);
  console.log("Readstream "+readStream);
  const gzip = zlib.createGzip();
  const cipher = crypto.createCipheriv('aes256', CIPHER_KEY, initVect);
  const appendInitVect = new AppendInitVect(initVect);
  // Create a write stream with a different file extension.
  const writeStream = fs.createWriteStream(path.join(file+"new"));
  console.log("writestream "+writeStream);
  readStream
    //.pipe(gzip)
    //.pipe(cipher)
   // .pipe(appendInitVect)
    .pipe(writeStream)
   // .on('error',function(err) {
   //   console.log(err);
    //})
    .on('finish', () => {
      console.log("write success")
      // The file upload is complete.
    });

  }


// [START generateThumbnail]
// [START generateThumbnailTrigger]

exports.generateThumbnail = functions.storage.object().onFinalize(async (object) => {


  const fileBucket = object.bucket; // The Storage bucket that contains the file.
  const filePath = object.name; // File path in the bucket.
  const contentType = object.contentType; // File content type.
  const metageneration = object.metageneration; // Number of times metadata has been generated. New objects have a value of 1.
  const destFileName="file_enc";
  const storage = new Storage();
  const fileName = path.basename(filePath);

  
  const bucket = admin.storage().bucket(fileBucket);
  const tempFilePath = path.join(os.tmpdir(), fileName);
  const newPath=`mirror/${filePath}`;
  const thumbFileName = `thumb_${fileName}`;
  const thumbFilePath = path.join(path.dirname(newPath), thumbFileName);
  
  const metadata = {
    contentType: contentType,
  };

  if(fileName.startsWith("thumb_")){
    return console.log("Already encrypted")
  }
  
  await bucket.file(filePath).download({destination: tempFilePath});
  console.log('Image downloaded locally to', tempFilePath);

  async function uploadEncryptedFile() {
  
    const options = {
      // The path to which the file should be uploaded, e.g. "file_encrypted.txt"
      destination: thumbFilePath,
      // Encrypt the file with a customer-supplied key.
      // See the "Generating your own encryption key" section above.
      encryptionKey: Buffer.from(crypto.randomBytes(32), 'base64'),
    };

    await storage.bucket(fileBucket).upload(tempFilePath, options);

    console.log(
      `File ${tempFilePath} uploaded to gs://${fileBucket}/${destFileName}.`
    );
    }
    uploadEncryptedFile().catch(console.error);
    return fs.unlinkSync(tempFilePath);
});  




// [END generateThumbnailTrigger]
  // [START eventAttributes]
/*
  const fileBucket = object.bucket; // The Storage bucket that contains the file.
  const filePath = object.name; // File path in the bucket.
  const contentType = object.contentType; // File content type.
  const metageneration = object.metageneration; // Number of times metadata has been generated. New objects have a value of 1.
  const newPath=`mirror/${filePath}`;  
  console.log("new path =" +newPath);
  const fileName = path.basename(filePath);
  // Exit if the image is already a thumbnail.
  if (fileName.startsWith('thumb_')) {
    return console.log('Already a Thumbnail.');
  }
  // [END stopConditions]

  // [START thumbnailGeneration]
  // Download file from bucket.
  const bucket = admin.storage().bucket(fileBucket);
  const tempFilePath = path.join(os.tmpdir(), fileName);
  const metadata = {
    contentType: contentType,
  };
  await bucket.file(filePath).download({destination: tempFilePath});
  console.log('Image downloaded locally to', tempFilePath);
  const newTempPath=path.join(tempFilePath+".enc");
  console.log("New temp path "+newTempPath);
  // We add a 'thumb_' prefix to thumbnails file name. That's where we'll upload the thumbnail.
  //encrypt({file:tempFilePath,password:"123456"});
  const readStream = fs.createReadStream(tempFilePath);
  const writeStream = fs.createWriteStream(path.join(tempFilePath+'new'));
  readStream
  .pipe(writeStream)
  .on('finish', () => {
  console.log("write success");
  const newFileBucket = object.bucket;
  const newBucket = admin.storage().bucket(newFileBucket);
  const thumbFileName = `thumb_${fileName}`;
  const thumbFilePath = path.join(path.dirname(newPath), thumbFileName);

  console.log("Thumb file name "+thumbFileName+" thumb file path "+thumbFilePath);
  const enPath=thumbFilePath+'.enc';
   // Uploading the thumbnail.
   newBucket.upload(tempFilePath+"new", {
    destination: thumbFilePath,
    metadata: metadata,
  });
  
});

 

/*
  const thumbFileName = `thumb_${fileName}`;
  const thumbFilePath = path.join(path.dirname(newPath), thumbFileName);

  console.log("Thumb file name "+thumbFileName+" thumb file path "+thumbFilePath);
  const enPath=thumbFilePath+'.enc';
    // Uploading the thumbnail.
  await bucket.upload(tempFilePath+"new", {
    destination: thumbFilePath,
    metadata: metadata,
  });
  */
  

  // Once the thumbnail has been uploaded delete the local file to free up disk space.
//  return fs.unlinkSync(tempFilePath);

  






/*
  // [END eventAttributes]

  // [START stopConditions]
  // Exit if this is triggered on a file that is not an image.


  // Get the file name.
  const fileName = path.basename(filePath);
  // Exit if the image is already a thumbnail.
  if (fileName.startsWith('thumb_')) {
    return console.log('Already a Thumbnail.');
  }
  // [END stopConditions]

  // [START thumbnailGeneration]
  // Download file from bucket.
  const bucket = admin.storage().bucket(fileBucket);
  const tempFilePath = path.join(os.tmpdir(), fileName);
  const metadata = {
    contentType: contentType,
  };
  await bucket.file(filePath).download({destination: tempFilePath});
  console.log('Image downloaded locally to', tempFilePath);
  // We add a 'thumb_' prefix to thumbnails file name. That's where we'll upload the thumbnail.
  const thumbFileName = `thumb_${fileName}`;
  const thumbFilePath = path.join(path.dirname(newPath), thumbFileName);
  // Uploading the thumbnail.
  await bucket.upload(tempFilePath, {
    destination: thumbFilePath,
    metadata: metadata,
  });

  // Once the thumbnail has been uploaded delete the local file to free up disk space.
  return fs.unlinkSync(tempFilePath);
  // [END thumbnailGeneration]

  */

