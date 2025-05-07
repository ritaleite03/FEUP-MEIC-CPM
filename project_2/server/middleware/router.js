const KoaRouter = require("koa-router");
const router = new KoaRouter();
const key = "SZQ66S6TCYALHMQWWXQ8QYVKG";

(async () => {
    try {
        router.post("/weather/city/all", getCityWeather);
    } catch (error) {}
})();

router.post("/weather/city/all", getCityWeather);

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
    const url = `https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/${cityFormat}/${datePastFormat}/${dateNextFormat}?key=${key}`;

    try {
        const response = await fetch(url);
        const data = await response.json();
        // console.log("Complete response from API Visual Crossing:");
        // console.log(data);

        const tomorrow = parseWeatherDay(data.days[8]);
        const today = parseWeatherDay(data.days[7]);
        const week = [
            parseWeatherDay(data.days[0]),
            parseWeatherDay(data.days[1]),
            parseWeatherDay(data.days[2]),
            parseWeatherDay(data.days[3]),
            parseWeatherDay(data.days[4]),
            parseWeatherDay(data.days[5]),
            parseWeatherDay(data.days[6]),
        ];
        ctx.status = 200;
        ctx.body = { tomorrow: tomorrow, today: today, week: week };
    } catch (error) {
        console.error("Error:", error);
        ctx.status = 200; // 400;

        ctx.body = {
            tomorrow: {
                temperature: {
                    realNow: "12",
                    realMax: "16",
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
                    dir: parseWindDirection(180),
                },
                info: {
                    icon: "rain",
                    cond: "rain",
                    desc: "rain",
                },
            },
            today: {
                temperature: {
                    realNow: 12,
                    realMax: 16,
                    realMin: 10,
                    feelMax: 22,
                    feelMin: 12,
                },
                precipitation: {
                    dimen: 1,
                    proba: 20,
                },
                wind: {
                    spd: 2,
                    dir: parseWindDirection(180),
                },
                info: {
                    icon: "rain",
                    cond: "rain",
                    desc: "rain",
                },
            },
            week: [
                {
                    temperature: {
                        realNow: 12,
                        realMax: 16,
                        realMin: 10,
                        feelMax: 22,
                        feelMin: 12,
                    },
                    precipitation: {
                        dimen: 1,
                        proba: 20,
                    },
                    wind: {
                        spd: 2,
                        dir: parseWindDirection(180),
                    },
                    info: {
                        icon: "rain",
                        cond: "rain",
                        desc: "rain",
                    },
                },
                {
                    temperature: {
                        realNow: 12,
                        realMax: 16,
                        realMin: 10,
                        feelMax: 22,
                        feelMin: 12,
                    },
                    precipitation: {
                        dimen: 1,
                        proba: 20,
                    },
                    wind: {
                        spd: 2,
                        dir: parseWindDirection(180),
                    },
                    info: {
                        icon: "rain",
                        cond: "rain",
                        desc: "rain",
                    },
                },
                {
                    temperature: {
                        realNow: 12,
                        realMax: 16,
                        realMin: 10,
                        feelMax: 22,
                        feelMin: 12,
                    },
                    precipitation: {
                        dimen: 1,
                        proba: 20,
                    },
                    wind: {
                        spd: 2,
                        dir: parseWindDirection(180),
                    },
                    info: {
                        icon: "rain",
                        cond: "rain",
                        desc: "rain",
                    },
                },
                {
                    temperature: {
                        realNow: 12,
                        realMax: 16,
                        realMin: 10,
                        feelMax: 22,
                        feelMin: 12,
                    },
                    precipitation: {
                        dimen: 1,
                        proba: 20,
                    },
                    wind: {
                        spd: 2,
                        dir: parseWindDirection(180),
                    },
                    info: {
                        icon: "rain",
                        cond: "rain",
                        desc: "rain",
                    },
                },
                {
                    temperature: {
                        realNow: 12,
                        realMax: 16,
                        realMin: 10,
                        feelMax: 22,
                        feelMin: 12,
                    },
                    precipitation: {
                        dimen: 1,
                        proba: 20,
                    },
                    wind: {
                        spd: 2,
                        dir: parseWindDirection(180),
                    },
                    info: {
                        icon: "rain",
                        cond: "rain",
                        desc: "rain",
                    },
                },
                {
                    temperature: {
                        realNow: 12,
                        realMax: 16,
                        realMin: 10,
                        feelMax: 22,
                        feelMin: 12,
                    },
                    precipitation: {
                        dimen: 1,
                        proba: 20,
                    },
                    wind: {
                        spd: 2,
                        dir: parseWindDirection(180),
                    },
                    info: {
                        icon: "rain",
                        cond: "rain",
                        desc: "rain",
                    },
                },
                {
                    temperature: {
                        realNow: 12,
                        realMax: 16,
                        realMin: 10,
                        feelMax: 22,
                        feelMin: 12,
                    },
                    precipitation: {
                        dimen: 1,
                        proba: 20,
                    },
                    wind: {
                        spd: 2,
                        dir: parseWindDirection(180),
                    },
                    info: {
                        icon: "rain",
                        cond: "rain",
                        desc: "rain",
                    },
                },
            ],
        };

        // ctx.body = {
        //     error: "Failed in getting city weather!",
        //     details: error.message,
        // };
    }
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
