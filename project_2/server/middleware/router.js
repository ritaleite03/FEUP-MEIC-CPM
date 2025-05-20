const KoaRouter = require("koa-router");
const { defaultDay, defaultTodayForecast } = require("./default_responses");
const router = new KoaRouter();
// const key = "SZQ66S6TCYALHMQWWXQ8QYVKG";
// const key = "KAZXLW5DUH5XZB5KDTCYUU7KZ";
// const key = "U2CH8GJDYSNXT9A3ZWU27VPKB";
const key = "Q5XXURLMXAKLA3EFKEQ9SAVS8";

(async () => {
    try {
        router.post("/weather/city/all", getCityWeather);
        router.post("/weather/city/today_tomorrow_forecast", getTodayAndTomorrowForecast)
    } catch (error) {}
})();

router.post("/weather/city/all", getCityWeather);

async function getTodayAndTomorrowForecast(ctx) {
    console.log("Getting today's forecast");
    
    const { city } = ctx.request.body;
    const cityFormat = city.trim() + ",PT";

    const url = `https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/${cityFormat}/today/tomorrow?key=${key}&unitGroup=metric&include=hours`;

    try {
        const respose = await fetch(url);
        const data = await respose.json();
        console.log(data);
        const today = data.days[0].hours.map(hour => ({
                "datetime": hour.datetime, 
                "temp": hour.temp,
                "icon": hour.icon
        }));
        const tomorrow = data.days[1].hours.map(hour => ({
                "datetime": hour.datetime, 
                "temp": hour.temp,
                "icon": hour.icon
        }));
        console.log("day aqui", today);
        ctx.status = 200;
        ctx.body = { today: today, tomorrow: tomorrow };
    } catch (error) {
        ctx.status = 200;
        ctx.body = { day: defaultTodayForecast };
        console.error("Error:", error);
    }
}

async function getCityWeather(ctx) {
    console.log("Getting city's weather now...");
    const { city } = ctx.request.body;

    // build data needed for request
    const date = new Date();
    const dateNext = new Date();
    const datePast = new Date();
    dateNext.setDate(date.getDate() + 1);
    datePast.setDate(date.getDate() - 7);

    // format data needed for request
    const cityFormat = city + ",PT";
    const dateNextFormat = parseDate(dateNext);
    const datePastFormat = parseDate(datePast);

    // build url for request
    const url = `https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/${cityFormat}/${datePastFormat}/${dateNextFormat}?key=${key}&unitGroup=metric`;

    // try {
    //     const response = await fetch(url);
    //     const data = await response.json();
    //     console.log("Complete response from API Visual Crossing:");
    //     // console.log(data);

    //     const tomorrow = parseWeatherDay(data.days[8]);
    //     const today = parseWeatherDay(data.days[7]);
    //     const week = [
    //         parseWeatherDay(data.days[0]),
    //         parseWeatherDay(data.days[1]),
    //         parseWeatherDay(data.days[2]),
    //         parseWeatherDay(data.days[3]),
    //         parseWeatherDay(data.days[4]),
    //         parseWeatherDay(data.days[5]),
    //         parseWeatherDay(data.days[6]),
    //     ];
    //     ctx.status = 200;
    //     ctx.body = { tomorrow: tomorrow, today: today, week: week };
    // } catch (error) {
    //     console.error("Error:", error);
        ctx.status = 200; // 400;

        ctx.body = {
            tomorrow: defaultDay,
            today: defaultDay,
            week: [
                defaultDay, // 1
                defaultDay, // 2
                defaultDay, // 3
                defaultDay, // 4
                defaultDay, // 5
                defaultDay, // 6
                defaultDay, // 7
            ],
        };

        // ctx.body = {
        //     error: "Failed in getting city weather!",
        //     details: error.message,
        // };
    // }
}

function parseDate(date) {
    return (
        date.getFullYear() +
        "-" +
        String(date.getMonth() + 1).padStart(2, "0") +
        "-" +
        String(date.getDate()).padStart(2, "0")
    );
}

function parseWeatherDay(day) {
    return {
        temperature: {
            realNow: day.temp,
            realMax: day.tempmax,
            realMin: day.tempmin,
            feelMax: day.feelslikemax,
            feelMin: day.feelslikemin,
        },
        precipitation: {
            dimen: day.precip,
            proba: day.precipprob,
        },
        wind: {
            spd: day.windspeed,
            dir: parseWindDirection(day.winddir),
        },
        sun: {
            rad: day.solarradiation,
            uvi: day.uvindex,
        },
        pressure: {
            pres: day.pressure,
        },
        humidity: {
            hum: day.humidity,
        },
        info: {
            icon: day.icon,
            cond: day.conditions,
            desc: day.description,
        },
    };
}

function parseWindDirection(direction) {
    if (direction > 22.5 && direction <= 67.5) return "NE";
    if (direction > 67.5 && direction <= 112.5) return "E";
    if (direction > 112.5 && direction <= 157.5) return "SE";
    if (direction > 157.5 && direction <= 202.5) return "S";
    if (direction > 202.5 && direction <= 247.5) return "SW";
    if (direction > 247.5 && direction <= 292.5) return "W";
    if (direction > 292.5 && direction <= 337.5) return "NW";
    if (direction > 337.5 || direction <= 22.5) return "N";
}

module.exports = router;
