package com.batch.demo.part4;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@Entity
@NoArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    
    @Enumerated(EnumType.STRING)
    private Level level = Level.NORMAL;
    
    private int totalAmount;
    
    private LocalDate updatedDate;
    
    @Builder   // 생성자를 기준으로 builder 객체 제공
    public User(String username, int totalAmount) {
        this.username = username;
        this.totalAmount = totalAmount;
    }

    public Level levelUp(){
        Level nextLevel = Level.getNextLevel(this.getTotalAmount());

        this.level = nextLevel;
        this.updatedDate = LocalDate.now();

        return nextLevel;
    }

    public boolean availableLevelUp() {
        return Level.availableLevelUp(this.getLevel(), this.getTotalAmount());
    }

    public enum Level {
        VIP(500_000, null),
        GOLD(500_000, VIP),
        SILVER(300_000, GOLD),
        NORMAL(200_000, SILVER);

        private final int nextAmount;
        private final Level nextLevel;

        Level(int nextAmount, Level nextLevel) {
            this.nextAmount = nextAmount;
            this.nextLevel = nextLevel;
        }

        private static boolean availableLevelUp(Level level, int totalAmount) {
            if(Objects.isNull(level)){
                return false;
            }
            // nextLevel이 null인 경우는 VIP뿐임
            if(Objects.isNull(level.nextLevel)){
                return false;
            }

            return totalAmount >= level.nextAmount;
        }

        private static Level getNextLevel(int totalAmount) {
            if(totalAmount >= Level.VIP.nextAmount){
                return VIP;
            }
            if(totalAmount >= Level.GOLD.nextAmount){
                return GOLD.nextLevel;
            }
            if(totalAmount >= Level.SILVER.nextAmount){
                return SILVER.nextLevel;
            }
            if(totalAmount >= Level.NORMAL.nextAmount){
                return NORMAL.nextLevel;
            }
            return NORMAL;
        }
    }
    
}
