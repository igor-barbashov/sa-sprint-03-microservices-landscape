package ru.yandex.practicum.smarthome.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.smarthome.dto.HeatingSystemDto;
import ru.yandex.practicum.smarthome.entity.HeatingSystem;
import ru.yandex.practicum.smarthome.repository.HeatingSystemRepository;

import java.util.Properties;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

@Service
@RequiredArgsConstructor
public class HeatingSystemServiceImpl implements HeatingSystemService {
    private final HeatingSystemRepository heatingSystemRepository;
    
    @Override
    public HeatingSystemDto getHeatingSystem(Long id) {
        HeatingSystem heatingSystem = heatingSystemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("HeatingSystem not found"));
        return convertToDto(heatingSystem);
    }

    @Override
    public HeatingSystemDto createHeatingSystem(HeatingSystemDto heatingSystemDto) {
        HeatingSystem entity = new HeatingSystem();
        entity.setTargetTemperature(heatingSystemDto.getTargetTemperature());
        entity.setOn(false);
        HeatingSystem savedHeatingSystem = heatingSystemRepository.save(entity);

        // send post into Kafka topic
        Properties props = new Properties();
        props.put("bootstrap.servers", "host.docker.internal:9091,host.docker.internal:9092,host.docker.internal:9093");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        ProducerRecord<String, String> record = new ProducerRecord<>("weather-app", "sensor1", "temperature=25,humidity=60");
        producer.send(record);
        producer.close();
        // send post into Kafka topic

        return convertToDto(savedHeatingSystem);
    }

    @Override
    public HeatingSystemDto updateHeatingSystem(Long id, HeatingSystemDto heatingSystemDto) {
        HeatingSystem existingHeatingSystem = heatingSystemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("HeatingSystem not found"));
        existingHeatingSystem.setOn(heatingSystemDto.isOn());
        existingHeatingSystem.setTargetTemperature(heatingSystemDto.getTargetTemperature());
        HeatingSystem updatedHeatingSystem = heatingSystemRepository.save(existingHeatingSystem);
        return convertToDto(updatedHeatingSystem);
    }

    @Override
    public void turnOn(Long id) {
        HeatingSystem heatingSystem = heatingSystemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("HeatingSystem not found"));
        heatingSystem.setOn(true);
        heatingSystemRepository.save(heatingSystem);
    }

    @Override
    public void turnOff(Long id) {
        HeatingSystem heatingSystem = heatingSystemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("HeatingSystem not found"));
        heatingSystem.setOn(false);
        heatingSystemRepository.save(heatingSystem);
    }

    @Override
    public void setTargetTemperature(Long id, double temperature) {
        HeatingSystem heatingSystem = heatingSystemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("HeatingSystem not found"));
        heatingSystem.setTargetTemperature(temperature);
        heatingSystemRepository.save(heatingSystem);
    }

    @Override
    public Double getCurrentTemperature(Long id) {
        HeatingSystem heatingSystem = heatingSystemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("HeatingSystem not found"));
        return heatingSystem.getCurrentTemperature();
    }

    private HeatingSystemDto convertToDto(HeatingSystem heatingSystem) {
        HeatingSystemDto dto = new HeatingSystemDto();
        dto.setId(heatingSystem.getId());
        dto.setOn(heatingSystem.isOn());
        dto.setTargetTemperature(heatingSystem.getTargetTemperature());
        return dto;
    }
}