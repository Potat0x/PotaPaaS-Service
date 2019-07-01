FROM node:8

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .

# application.properties -> default_webapp_port
ENV PORT 5005

CMD [ "npm", "start" ]
