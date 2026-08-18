import axios from 'axios';

const axiosClient = axios.create({
    // baseURL: 'http://localhost/api',    //for server present locally
    baseURL: '/api',           // for ec2 server
});

axiosClient.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

axiosClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (
            error.response?.status === 401 &&
            !error.config?.url?.includes('/auth/login') &&
            !error.config?.url?.includes('/auth/register')
        ) {
            localStorage.clear();
            window.location.href = '/login';
        }

        return Promise.reject(error);
    }
);

export default axiosClient;