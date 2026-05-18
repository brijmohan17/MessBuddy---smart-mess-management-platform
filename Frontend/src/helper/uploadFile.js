const useBackend = import.meta.env.VITE_USE_BACKEND_UPLOAD === 'true';
const cloudinaryUrl = `https://api.cloudinary.com/v1_1/${import.meta.env.VITE_CLOUDINARY_CLOUD_NAME}/auto/upload`;

const uploadFile = async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('upload_preset', 'BuzzChat');

    if (useBackend) {
        const backendUrl = `${import.meta.env.VITE_BACKEND_URL}/api/upload`;
        const response = await fetch(backendUrl, {
            method: 'POST',
            body: formData,
            credentials: 'include'
        });
        return await response.json();
    }

    const response = await fetch(cloudinaryUrl, {
        method: 'POST',
        body: formData
    });
    const responseData = await response.json();
    return responseData;
};

export default uploadFile;