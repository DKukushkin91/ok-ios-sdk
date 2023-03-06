import axios from 'axios';
import extractSync from 'extract-zip';
import fs from 'fs-extra';
import process from 'process';

const SDK_ZIP = 'ok-ios-sdk.zip';
const SDK_DIR = 'ios/ok-ios-sdk/';
const SDK_DOWNLOAD_URL = 'https://api.github.com/repos/odnoklassniki/ok-ios-sdk/zipball/master';

export default async function downloadSdk() {
    const alreadyDownloaded = await fs.pathExistsSync(SDK_DIR);
    if (alreadyDownloaded) {
        console.log('OK iOS SDK already downloaded.');
        return;
    }

    console.log(`Downloading OK iOS SDK as ${SDK_ZIP}.`);
    const zip = await axios.get(SDK_DOWNLOAD_URL, { responseType: 'arraybuffer' });
    await fs.writeFile(SDK_ZIP, zip.data);

    console.log(`Extracting OK iOS SDK to ${SDK_DIR}.`);
    try {
        const unzipDir = await new Promise((resolve, reject) => {
            let extDir = '';
            extractSync(
                SDK_ZIP,
                {
                    onEntry: (entry) => extDir = extDir || entry.fileName,
                    dir: process.cwd(),
                },
                (err) => err ? reject(err) : resolve(extDir),
            );
        });
        await fs.rename(unzipDir, SDK_DIR);
        console.log('Done.');
    } catch (err) {
        console.warn(`Failed to download and extract OK iOS SDK: ${err}.`);
    } finally {
        try {
            await fs.remove(SDK_ZIP);
        } catch (e) {
            console.warn('Failed to unlink some temporary/unnecessary files.');
        }
    }
}

downloadSdk();
