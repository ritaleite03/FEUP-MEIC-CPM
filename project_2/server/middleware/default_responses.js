const defaultDay = {
    temperature: {
        realNow: "13",
        realMax: "17",
        realMin: "10",
        feelMax: "22",
        feelMin: "12",
    },
    precipitation: {
        dimen: "1",
        proba: "20",
    },
    wind: {
        spd: "2",
        dir: "S",
    },
    sun: {
        rad: 140,
        uvi: 3,
    },
    pressure: {
        pres: 13,
    },
    humidity: {
        hum: 14,
    },
    info: {
        icon: "rain",
        cond: "rain",
        desc: "rain, rain, rain, rain, rain, rain, rain, rain, rain, rain, rain",
    },
};

const defaultTodayForecast = [
    { datetime: "00:00:00", temp: 16, icon: "partly-cloudy-night" },
    { datetime: "01:00:00", temp: 16, icon: "partly-cloudy-night" },
    { datetime: "02:00:00", temp: 16, icon: "partly-cloudy-night" },
    { datetime: "03:00:00", temp: 15, icon: "partly-cloudy-night" },
    { datetime: "04:00:00", temp: 15.2, icon: "partly-cloudy-night" },
    { datetime: "05:00:00", temp: 15.2, icon: "partly-cloudy-night" },
    { datetime: "06:00:00", temp: 15.2, icon: "partly-cloudy-night" },
    { datetime: "07:00:00", temp: 15.2, icon: "partly-cloudy-day" },
    { datetime: "08:00:00", temp: 16.1, icon: "partly-cloudy-day" },
    { datetime: "09:00:00", temp: 18.1, icon: "partly-cloudy-day" },
    { datetime: "10:00:00", temp: 19.9, icon: "partly-cloudy-day" },
    { datetime: "11:00:00", temp: 20.2, icon: "partly-cloudy-day" },
    { datetime: "12:00:00", temp: 22.1, icon: "partly-cloudy-day" },
    { datetime: "13:00:00", temp: 22.3, icon: "partly-cloudy-day" },
    { datetime: "14:00:00", temp: 23.2, icon: "partly-cloudy-day" },
    { datetime: "15:00:00", temp: 23.2, icon: "partly-cloudy-day" },
    { datetime: "16:00:00", temp: 22.6, icon: "partly-cloudy-day" },
    { datetime: "17:00:00", temp: 23.2, icon: "partly-cloudy-day" },
    { datetime: "18:00:00", temp: 22.4, icon: "partly-cloudy-day" },
    { datetime: "19:00:00", temp: 20.6, icon: "partly-cloudy-day" },
    { datetime: "20:00:00", temp: 19.4, icon: "partly-cloudy-day" },
    { datetime: "21:00:00", temp: 18.6, icon: "clear-night" },
    { datetime: "22:00:00", temp: 17.7, icon: "clear-night" },
    { datetime: "23:00:00", temp: 17.4, icon: "partly-cloudy-night" },
];

const defaultTomorrowForecast = [
    { datetime: "00:00:00", temp: 14.8, icon: "partly-cloudy-night" },
    { datetime: "01:00:00", temp: 14.7, icon: "partly-cloudy-night" },
    { datetime: "02:00:00", temp: 14.8, icon: "partly-cloudy-night" },
    { datetime: "03:00:00", temp: 14.8, icon: "partly-cloudy-night" },
    { datetime: "04:00:00", temp: 14.6, icon: "partly-cloudy-night" },
    { datetime: "05:00:00", temp: 14.7, icon: "partly-cloudy-night" },
    { datetime: "06:00:00", temp: 14.6, icon: "partly-cloudy-night" },
    { datetime: "07:00:00", temp: 14.9, icon: "partly-cloudy-day" },
    { datetime: "08:00:00", temp: 15.9, icon: "cloudy" },
    { datetime: "09:00:00", temp: 17, icon: "partly-cloudy-day" },
    { datetime: "10:00:00", temp: 18.6, icon: "partly-cloudy-day" },
    { datetime: "11:00:00", temp: 20.1, icon: "partly-cloudy-day" },
    { datetime: "12:00:00", temp: 21.8, icon: "cloudy" },
    { datetime: "13:00:00", temp: 22.5, icon: "partly-cloudy-day" },
    { datetime: "14:00:00", temp: 23.1, icon: "clear-day" },
    { datetime: "15:00:00", temp: 23.4, icon: "clear-day" },
    { datetime: "16:00:00", temp: 22.5, icon: "cloudy" },
    { datetime: "17:00:00", temp: 21.5, icon: "partly-cloudy-day" },
    { datetime: "18:00:00", temp: 20.4, icon: "cloudy" },
    { datetime: "19:00:00", temp: 19.2, icon: "partly-cloudy-day" },
    { datetime: "20:00:00", temp: 18.3, icon: "partly-cloudy-day" },
    { datetime: "21:00:00", temp: 17.1, icon: "partly-cloudy-night" },
    { datetime: "22:00:00", temp: 16.6, icon: "partly-cloudy-night" },
    { datetime: "23:00:00", temp: 16.4, icon: "partly-cloudy-night" },
];

module.exports = { defaultDay, defaultTodayForecast, defaultTomorrowForecast };
